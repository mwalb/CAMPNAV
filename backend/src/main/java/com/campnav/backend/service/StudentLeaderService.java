package com.campnav.backend.service;

import com.campnav.backend.dto.StudentLeaderDTO;
import com.campnav.backend.model.*;
import com.campnav.backend.model.StudentLeaderAssignment.AssignmentStatus;
import com.campnav.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentLeaderService {

    private final StudentLeaderAssignmentRepository leaderAssignmentRepository;
    private final CommunityUserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final IssueReportRepository reportRepository;
    private final UniversityOfficeReferralRepository referralRepository;
    private final AdminActivityLogRepository activityLogRepository;

    @Transactional
    public StudentLeaderDTO appointStudentLeader(Long universityId, String fullName, String email, String leaderUsername, String adminUsername) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new RuntimeException("University not found with ID: " + universityId));

        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        String normalizedFullName = fullName != null ? fullName.trim() : "";
        String targetUsername = leaderUsername != null && !leaderUsername.trim().isEmpty()
                ? leaderUsername.trim().toLowerCase()
                : (normalizedEmail.contains("@") ? normalizedEmail.substring(0, normalizedEmail.indexOf("@")) : "leader_" + (university.getShortName() != null ? university.getShortName().toLowerCase() : "univ"));

        // Find existing user by email or username, or create a new user
        CommunityUser leaderUser = userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> userRepository.findByUsername(targetUsername)
                        .orElseGet(() -> CommunityUser.builder()
                                .externalId("LEADER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                                .username(targetUsername)
                                .email(normalizedEmail)
                                .fullName(normalizedFullName)
                                .passwordHash("$2a$10$7R.x5VbZ9E/3l3V3/1x.0uKj4b9b9b9b9b9b9b9b9b9b9b9b9")
                                .isActive(true)
                                .reputationScore(50)
                                .build()));

        if (!normalizedFullName.isEmpty()) leaderUser.setFullName(normalizedFullName);
        if (!normalizedEmail.isEmpty()) leaderUser.setEmail(normalizedEmail);
        leaderUser.setRole(CommunityUser.UserRole.STUDENT_LEADER);
        leaderUser.setIsActive(true);
        userRepository.save(leaderUser);

        CommunityUser adminUser = adminUsername != null ? userRepository.findByUsername(adminUsername).orElse(null) : null;

        // Deactivate previous active leader assignment for this university
        Optional<StudentLeaderAssignment> existingActive = leaderAssignmentRepository
                .findFirstByUniversityIdAndStatusOrderByIdDesc(universityId, AssignmentStatus.ACTIVE);

        if (existingActive.isPresent()) {
            StudentLeaderAssignment prev = existingActive.get();
            prev.setStatus(AssignmentStatus.REPLACED);
            prev.setEndDate(LocalDateTime.now());
            prev.setDeactivationReason("Replaced by new leader appointment: " + leaderUser.getUsername());
            leaderAssignmentRepository.save(prev);
        }

        // Create new active assignment
        StudentLeaderAssignment newAssignment = StudentLeaderAssignment.builder()
                .leader(leaderUser)
                .university(university)
                .status(AssignmentStatus.ACTIVE)
                .appointedByAdmin(adminUser)
                .appointmentDate(LocalDateTime.now())
                .verificationDate(LocalDateTime.now())
                .startDate(LocalDateTime.now())
                .build();

        StudentLeaderAssignment saved = leaderAssignmentRepository.save(newAssignment);

        // Re-route any unassigned reports for this university to the new leader
        List<IssueReport> unassignedReports = reportRepository.findByUniversityIdAndRoutingStatus(
                universityId, IssueReport.RoutingStatus.UNASSIGNED
        );
        for (IssueReport report : unassignedReports) {
            report.setAssignedLeader(leaderUser);
            report.setRoutingStatus(IssueReport.RoutingStatus.ASSIGNED_TO_LEADER);
            reportRepository.save(report);
        }

        AdminActivityLog logEvent = AdminActivityLog.builder()
                .action("STUDENT_LEADER_APPOINTED")
                .actorUsername(adminUsername != null ? adminUsername : "admin")
                .description("Appointed " + leaderUser.getFullName() + " (" + leaderUser.getEmail() + ") as Student Leader for " + university.getName())
                .relatedEntityId("UNIV-" + universityId)
                .build();
        activityLogRepository.save(logEvent);

        return toDTO(saved);
    }

    @Transactional
    public StudentLeaderDTO appointStudentLeader(Long universityId, String leaderUsername, String adminUsername) {
        CommunityUser user = userRepository.findByUsername(leaderUsername)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + leaderUsername));
        return appointStudentLeader(universityId, user.getFullName(), user.getEmail(), user.getUsername(), adminUsername);
    }

    @Transactional
    public StudentLeaderDTO updateAssignmentStatus(Long assignmentId, AssignmentStatus newStatus, String reason, String adminUsername) {
        StudentLeaderAssignment assignment = leaderAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + assignmentId));

        assignment.setStatus(newStatus);
        assignment.setUpdatedAt(LocalDateTime.now());
        if (newStatus == AssignmentStatus.DEACTIVATED || newStatus == AssignmentStatus.SUSPENDED) {
            assignment.setEndDate(LocalDateTime.now());
            if (reason != null) assignment.setDeactivationReason(reason);
        }

        StudentLeaderAssignment saved = leaderAssignmentRepository.save(assignment);

        AdminActivityLog logEvent = AdminActivityLog.builder()
                .action("STUDENT_LEADER_STATUS_CHANGED")
                .actorUsername(adminUsername != null ? adminUsername : "admin")
                .description("Changed Student Leader assignment ID " + assignmentId + " status to " + newStatus)
                .relatedEntityId("LEADER-" + assignmentId)
                .build();
        activityLogRepository.save(logEvent);

        return toDTO(saved);
    }

    public Optional<CommunityUser> getActiveLeaderForUniversity(Long universityId) {
        return leaderAssignmentRepository.findFirstByUniversityIdAndStatusOrderByIdDesc(universityId, AssignmentStatus.ACTIVE)
                .map(StudentLeaderAssignment::getLeader);
    }

    public List<Long> getAuthorizedUniversityIdsForUser(String username) {
        CommunityUser user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return Collections.emptyList();

        if (user.getRole() == CommunityUser.UserRole.ADMIN) {
            return universityRepository.findAll().stream().map(University::getId).toList();
        }

        return leaderAssignmentRepository.findByLeaderUsernameAndStatus(username, AssignmentStatus.ACTIVE)
                .stream()
                .map(a -> a.getUniversity().getId())
                .toList();
    }

    public Map<String, Object> getLeaderOverviewStats(String username) {
        List<Long> univIds = getAuthorizedUniversityIdsForUser(username);
        if (univIds.isEmpty()) return Collections.emptyMap();

        List<IssueReport> allAssigned = new ArrayList<>();
        for (Long univId : univIds) {
            allAssigned.addAll(reportRepository.findByUniversityId(univId));
        }

        long total = allAssigned.size();
        long newReports = allAssigned.stream().filter(r -> r.getRoutingStatus() == IssueReport.RoutingStatus.ASSIGNED_TO_LEADER).count();
        long awaitingAck = allAssigned.stream().filter(r -> r.getRoutingStatus() == IssueReport.RoutingStatus.ASSIGNED_TO_LEADER || r.getStatus() == IssueReport.IssueStatus.SUBMITTED).count();
        long inProgress = allAssigned.stream().filter(r -> r.getStatus() == IssueReport.IssueStatus.IN_PROGRESS).count();
        long referred = allAssigned.stream().filter(r -> r.getRoutingStatus() == IssueReport.RoutingStatus.REFERRED_TO_UNIVERSITY).count();
        long awaitingResp = allAssigned.stream().filter(r -> r.getRoutingStatus() == IssueReport.RoutingStatus.AWAITING_RESPONSE).count();
        long resolved = allAssigned.stream().filter(r -> r.getStatus() == IssueReport.IssueStatus.RESOLVED || r.getStatus() == IssueReport.IssueStatus.COMMUNITY_VERIFIED).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReports", total);
        stats.put("newReports", newReports);
        stats.put("awaitingAcknowledgement", awaitingAck);
        stats.put("inProgress", inProgress);
        stats.put("referredToOffice", referred);
        stats.put("awaitingResponse", awaitingResp);
        stats.put("resolvedReports", resolved);
        return stats;
    }

    public List<StudentLeaderDTO> getAllAssignments() {
        return leaderAssignmentRepository.findAll().stream().map(this::toDTO).toList();
    }

    public StudentLeaderDTO toDTO(StudentLeaderAssignment a) {
        return StudentLeaderDTO.builder()
                .id(a.getId())
                .leaderUserId(a.getLeader().getId())
                .leaderUsername(a.getLeader().getUsername())
                .leaderFullName(a.getLeader().getFullName())
                .leaderEmail(a.getLeader().getEmail())
                .leaderPhone(a.getLeader().getPhone())
                .universityId(a.getUniversity().getId())
                .universityName(a.getUniversity().getName())
                .status(a.getStatus().name())
                .appointedByAdminUsername(a.getAppointedByAdmin() != null ? a.getAppointedByAdmin().getUsername() : "admin")
                .appointmentDate(a.getAppointmentDate())
                .startDate(a.getStartDate())
                .endDate(a.getEndDate())
                .deactivationReason(a.getDeactivationReason())
                .build();
    }
}
