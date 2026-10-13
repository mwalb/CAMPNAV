package com.campnav.backend.service;

import com.campnav.backend.model.*;
import com.campnav.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueReportService {
    private final IssueReportRepository reportRepository;
    private final DepartmentRepository departmentRepository;
    private final UniversityRepository universityRepository;
    private final StudentLeaderService studentLeaderService;
    private final EmailNotificationService emailNotificationService;
    private final UniversityOfficeReferralRepository referralRepository;
    private final AdminActivityLogRepository activityLogRepository;

    @Transactional
    public IssueReport createReport(IssueReport report) {
        report.setId(null); // Ensure JPA auto-generates a new primary key

        // 1. Generate Unique External ID in format CAM-YYYYMMDD-XXXXX
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String externalId = "CAM-" + dateStr + "-" + String.format("%05d", (int)(Math.random() * 90000) + 10000);
        report.setExternalId(externalId);

        // Reporter Identity Validation: Name is REQUIRED, Email is OPTIONAL
        if (report.getReporterName() == null || report.getReporterName().trim().isEmpty()) {
            throw new IllegalArgumentException("Reporter name is required");
        }
        report.setReporterName(report.getReporterName().trim());

        if (report.getReporterEmail() != null && !report.getReporterEmail().trim().isEmpty()) {
            String email = report.getReporterEmail().trim();
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("Invalid email format");
            }
            report.setReporterEmail(email);
        } else {
            report.setReporterEmail(null);
        }

        if (report.getStatus() == null) {
            report.setStatus(IssueReport.IssueStatus.SUBMITTED);
        }

        // 2. Attach University Entity if ID provided
        if (report.getUniversity() != null && report.getUniversity().getId() != null) {
            universityRepository.findById(report.getUniversity().getId()).ifPresent(report::setUniversity);
        } else if (report.getUniversity() == null) {
            // Default to first university (e.g. UDSM) if unassigned
            universityRepository.findAll().stream().findFirst().ifPresent(report::setUniversity);
        }

        // 3. Automated Priority & Impact Score Calculation
        if (report.getUserSeverity() != null) {
            report.setCalculatedPriority(calculatePriority(report));
        } else {
            report.setUserSeverity(IssueReport.IssuePriority.MEDIUM);
            report.setCalculatedPriority(IssueReport.IssuePriority.MEDIUM);
        }
        report.setImpactScore(calculateImpactScore(report));

        // 4. Automated Department Assignment
        assignDepartment(report);

        // 5. Leader Auto-Routing & Email Outbox Queuing
        if (report.getUniversity() != null) {
            Optional<CommunityUser> activeLeaderOpt = studentLeaderService.getActiveLeaderForUniversity(report.getUniversity().getId());
            if (activeLeaderOpt.isPresent()) {
                CommunityUser leader = activeLeaderOpt.get();
                report.setAssignedLeader(leader);
                report.setRoutingStatus(IssueReport.RoutingStatus.ASSIGNED_TO_LEADER);
                report.setNotificationStatus("QUEUED");
            } else {
                report.setRoutingStatus(IssueReport.RoutingStatus.UNASSIGNED);
                report.setNotificationStatus("UNASSIGNED_LEADER");
            }
        } else {
            report.setRoutingStatus(IssueReport.RoutingStatus.UNASSIGNED);
        }

        // 6. Initialize Timeline
        IssueTimeline initialEvent = IssueTimeline.builder()
                .issue(report)
                .status(report.getStatus())
                .message("Report successfully submitted and registered with reference " + externalId)
                .timestamp(LocalDateTime.now())
                .build();

        List<IssueTimeline> timeline = new ArrayList<>();
        timeline.add(initialEvent);
        report.setTimeline(timeline);
        report.setTimestamp(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        // Ensure valid default coordinates
        if (report.getLatitude() == null) report.setLatitude(-6.780543);
        if (report.getLongitude() == null) report.setLongitude(39.204593);

        IssueReport saved = reportRepository.save(report);

        // 7. Queue Dual Notifications (Leader AND Admin)
        if (saved.getAssignedLeader() != null) {
            emailNotificationService.queueNewReportLeaderNotification(saved, saved.getAssignedLeader());
            emailNotificationService.queueNewReportAdminNotification(saved);
        } else {
            emailNotificationService.queueMissingLeaderAdminNotification(saved);
        }

        // Log Activity
        AdminActivityLog logEvent = AdminActivityLog.builder()
                .action("ISSUE_CREATED")
                .actorUsername(saved.getReporter() != null ? saved.getReporter().getUsername() : "citizen")
                .description("New issue reported: " + saved.getExternalId() + " (" + saved.getCategory() + ") at " + (saved.getUniversity() != null ? saved.getUniversity().getName() : "Campus"))
                .relatedEntityId(saved.getExternalId())
                .build();
        activityLogRepository.save(logEvent);

        return saved;
    }

    @Transactional
    public EmailNotification resendLeaderNotificationByAdmin(String externalId, String adminUsername) {
        IssueReport report = getReportByExternalId(externalId);
        CommunityUser leader = report.getAssignedLeader();
        if (leader == null && report.getUniversity() != null) {
            leader = studentLeaderService.getActiveLeaderForUniversity(report.getUniversity().getId()).orElse(null);
        }
        if (leader == null) {
            throw new IllegalStateException("No active student leader assigned or available for " + (report.getUniversity() != null ? report.getUniversity().getName() : "this university"));
        }

        report.setAssignedLeader(leader);
        report.setRoutingStatus(IssueReport.RoutingStatus.ASSIGNED_TO_LEADER);
        report.setNotificationStatus("QUEUED");
        reportRepository.save(report);

        EmailNotification notification = emailNotificationService.queueNewReportLeaderNotification(report, leader);

        AdminActivityLog logEvent = AdminActivityLog.builder()
                .action("NOTIFICATION_RESENT_BY_ADMIN")
                .actorUsername(adminUsername != null ? adminUsername : "admin")
                .description("Admin resent notification for issue " + externalId + " to leader " + leader.getEmail())
                .relatedEntityId(externalId)
                .build();
        activityLogRepository.save(logEvent);

        return notification;
    }

    @Transactional
    public IssueReport acknowledgeReportByLeader(String externalId, String leaderUsername) {
        IssueReport report = getReportByExternalId(externalId);
        report.setRoutingStatus(IssueReport.RoutingStatus.LEADER_ACKNOWLEDGED);
        report.setStatus(IssueReport.IssueStatus.UNDER_REVIEW);
        report.setUpdatedAt(LocalDateTime.now());

        IssueTimeline event = IssueTimeline.builder()
                .issue(report)
                .status(IssueReport.IssueStatus.UNDER_REVIEW)
                .message("Report acknowledged by CAMPNAV Student Leader (" + leaderUsername + "). Follow-up initiated.")
                .timestamp(LocalDateTime.now())
                .build();

        if (report.getTimeline() == null) report.setTimeline(new ArrayList<>());
        report.getTimeline().add(event);

        IssueReport saved = reportRepository.save(report);

        if (report.getReporter() != null && report.getReporter().getEmail() != null) {
            emailNotificationService.queueStatusUpdateReporterNotification(
                    saved, report.getReporter().getEmail(), "Acknowledged by Student Leader", "Your report has been acknowledged by the student leader and is under active review."
            );
        }

        return saved;
    }

    @Transactional
    public UniversityOfficeReferral referReportToUniversityOffice(
            String externalId, String officeName, String officeContact, String channel, LocalDateTime followUpDate, String leaderUsername) {
        IssueReport report = getReportByExternalId(externalId);

        CommunityUser leader = report.getAssignedLeader();
        if (leader == null && leaderUsername != null) {
            // fallback
        }

        UniversityOfficeReferral referral = UniversityOfficeReferral.builder()
                .report(report)
                .referringLeader(leader)
                .officeName(officeName)
                .officeContact(officeContact)
                .referralChannel(channel != null ? channel : "OFFICIAL_COMMUNICATION")
                .referralDate(LocalDateTime.now())
                .officeAckStatus(UniversityOfficeReferral.OfficeAckStatus.PENDING_ACK)
                .followUpDate(followUpDate)
                .build();

        UniversityOfficeReferral savedReferral = referralRepository.save(referral);

        report.setRoutingStatus(IssueReport.RoutingStatus.REFERRED_TO_UNIVERSITY);
        report.setStatus(IssueReport.IssueStatus.IN_PROGRESS);
        report.setUpdatedAt(LocalDateTime.now());

        IssueTimeline event = IssueTimeline.builder()
                .issue(report)
                .status(IssueReport.IssueStatus.IN_PROGRESS)
                .message("Report officially referred to university office: " + officeName + " (" + channel + ")")
                .timestamp(LocalDateTime.now())
                .build();

        if (report.getTimeline() == null) report.setTimeline(new ArrayList<>());
        report.getTimeline().add(event);

        reportRepository.save(report);

        if (report.getReporter() != null && report.getReporter().getEmail() != null) {
            emailNotificationService.queueStatusUpdateReporterNotification(
                    report, report.getReporter().getEmail(), "Referred to University Office", "Your issue has been formally referred to " + officeName + " for resolution."
            );
        }

        return savedReferral;
    }

    @Transactional
    public IssueReport leaderResolveReport(String externalId, String resolutionDescription, String afterImageUrl, String leaderUsername) {
        IssueReport report = getReportByExternalId(externalId);
        report.setStatus(IssueReport.IssueStatus.RESOLVED);
        report.setPublicResponse(resolutionDescription);
        if (afterImageUrl != null) report.setAfterImageUrl(afterImageUrl);
        report.setUpdatedAt(LocalDateTime.now());

        IssueTimeline event = IssueTimeline.builder()
                .issue(report)
                .status(IssueReport.IssueStatus.RESOLVED)
                .message("Resolution recorded by Student Leader (" + leaderUsername + "): " + resolutionDescription)
                .timestamp(LocalDateTime.now())
                .build();

        if (report.getTimeline() == null) report.setTimeline(new ArrayList<>());
        report.getTimeline().add(event);

        IssueReport saved = reportRepository.save(report);

        if (report.getReporter() != null && report.getReporter().getEmail() != null) {
            emailNotificationService.queueStatusUpdateReporterNotification(
                    saved, report.getReporter().getEmail(), "Resolved", "Resolution details: " + resolutionDescription
            );
        }

        return saved;
    }

    private IssueReport.IssuePriority calculatePriority(IssueReport report) {
        if (Boolean.TRUE.equals(report.getIsEmergency())) return IssueReport.IssuePriority.CRITICAL;
        if ("PUBLIC_SAFETY".equalsIgnoreCase(report.getCategory()) || "ELECTRICITY".equalsIgnoreCase(report.getCategory())) {
            if (IssueReport.IssuePriority.HIGH.equals(report.getUserSeverity())) {
                return IssueReport.IssuePriority.CRITICAL;
            }
            return IssueReport.IssuePriority.HIGH;
        }
        return report.getUserSeverity() != null ? report.getUserSeverity() : IssueReport.IssuePriority.MEDIUM;
    }

    private Integer calculateImpactScore(IssueReport report) {
        int score = 20;
        if (Boolean.TRUE.equals(report.getIsEmergency())) score += 50;
        if (report.getUserSeverity() != null) {
            score += switch (report.getUserSeverity()) {
                case CRITICAL -> 40;
                case HIGH -> 30;
                case MEDIUM -> 20;
                case LOW -> 10;
            };
        }
        return Math.min(100, score);
    }

    private void assignDepartment(IssueReport report) {
        String cat = report.getCategory() != null ? report.getCategory().toUpperCase() : "OTHER";
        String deptName = switch (cat) {
            case "WATER", "WATER & SANITATION" -> "Water Authority";
            case "ROADS", "ROADS & TRANSPORT" -> "Roads Department";
            case "WASTE", "WASTE & ENVIRONMENT" -> "Waste Management";
            case "ELECTRICITY", "ELECTRICITY & POWER" -> "Electricity Board";
            case "PUBLIC_SAFETY", "SAFETY & SECURITY" -> "Public Safety";
            default -> "Municipal Works";
        };
        departmentRepository.findByName(deptName).ifPresent(report::setAssignedDepartment);
    }

    public List<IssueReport> getNearbyIssues(Double lat, Double lng, Double radiusKm) {
        return reportRepository.findNearbyIssues(lat, lng, radiusKm);
    }

    public List<IssueReport> getAllReports() {
        return reportRepository.findAll();
    }

    public List<IssueReport> getReportsByUniversity(Long universityId) {
        return reportRepository.findByUniversityId(universityId);
    }

    public IssueReport getReportByExternalId(String externalId) {
        return reportRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Issue not found with ID: " + externalId));
    }

    @Transactional
    public IssueReport confirmIssue(String externalId) {
        IssueReport report = getReportByExternalId(externalId);
        report.setSupportsCount((report.getSupportsCount() != null ? report.getSupportsCount() : 0) + 1);
        report.setUpdatedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    @Transactional
    public IssueReport updateStatus(String externalId, IssueReport.IssueStatus newStatus, String message, String internalNotes, String publicResponse, String adminUsername) {
        IssueReport report = getReportByExternalId(externalId);

        report.setStatus(newStatus);
        if (internalNotes != null) report.setInternalNotes(internalNotes);
        if (publicResponse != null) report.setPublicResponse(publicResponse);
        report.setUpdatedAt(LocalDateTime.now());

        IssueTimeline event = IssueTimeline.builder()
                .issue(report)
                .status(newStatus)
                .message(message != null ? message : "Status updated to " + newStatus)
                .timestamp(LocalDateTime.now())
                .build();

        if (report.getTimeline() == null) report.setTimeline(new ArrayList<>());
        report.getTimeline().add(event);

        IssueReport saved = reportRepository.save(report);

        AdminActivityLog log = AdminActivityLog.builder()
                .action("ISSUE_STATUS_CHANGED")
                .actorUsername(adminUsername != null ? adminUsername : "admin")
                .description("Updated issue " + externalId + " status to " + newStatus)
                .relatedEntityId(externalId)
                .build();
        activityLogRepository.save(log);

        return saved;
    }

    @Transactional
    public IssueReport assignIssue(String externalId, Long departmentId, String adminUsername) {
        IssueReport report = getReportByExternalId(externalId);
        departmentRepository.findById(departmentId).ifPresent(report::setAssignedDepartment);
        report.setStatus(IssueReport.IssueStatus.ASSIGNED);
        report.setUpdatedAt(LocalDateTime.now());

        IssueTimeline event = IssueTimeline.builder()
                .issue(report)
                .status(IssueReport.IssueStatus.ASSIGNED)
                .message("Assigned to department: " + (report.getAssignedDepartment() != null ? report.getAssignedDepartment().getName() : "Department"))
                .timestamp(LocalDateTime.now())
                .build();
        report.getTimeline().add(event);

        IssueReport saved = reportRepository.save(report);

        AdminActivityLog log = AdminActivityLog.builder()
                .action("ISSUE_ASSIGNED")
                .actorUsername(adminUsername != null ? adminUsername : "admin")
                .description("Assigned issue " + externalId + " to department")
                .relatedEntityId(externalId)
                .build();
        activityLogRepository.save(log);

        return saved;
    }

    @Transactional
    public IssueReport resolveIssue(String externalId, String resolutionDescription, String adminUsername) {
        IssueReport report = getReportByExternalId(externalId);
        report.setStatus(IssueReport.IssueStatus.RESOLVED);
        report.setPublicResponse(resolutionDescription);
        report.setUpdatedAt(LocalDateTime.now());

        IssueTimeline event = IssueTimeline.builder()
                .issue(report)
                .status(IssueReport.IssueStatus.RESOLVED)
                .message("Issue resolved: " + resolutionDescription)
                .timestamp(LocalDateTime.now())
                .build();
        report.getTimeline().add(event);

        IssueReport saved = reportRepository.save(report);

        AdminActivityLog log = AdminActivityLog.builder()
                .action("ISSUE_RESOLVED")
                .actorUsername(adminUsername != null ? adminUsername : "admin")
                .description("Resolved issue " + externalId)
                .relatedEntityId(externalId)
                .build();
        activityLogRepository.save(log);

        return saved;
    }

    @Transactional
    public IssueReport verifyResolution(String externalId, boolean resolved, String username) {
        IssueReport report = getReportByExternalId(externalId);
        String message = resolved ? "Community verified the resolution." : "Resident reported issue still exists.";
        IssueReport.IssueStatus status = resolved ? IssueReport.IssueStatus.COMMUNITY_VERIFIED : IssueReport.IssueStatus.IN_PROGRESS;

        report.setStatus(status);
        report.setVerificationCount((report.getVerificationCount() != null ? report.getVerificationCount() : 0) + 1);
        report.setUpdatedAt(LocalDateTime.now());

        IssueTimeline event = IssueTimeline.builder()
                .issue(report)
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        report.getTimeline().add(event);

        return reportRepository.save(report);
    }

    public Map<String, Object> getAdminStats() {
        List<IssueReport> reports = reportRepository.findAll();
        long total = reports.size();
        long submitted = reports.stream().filter(r -> r.getStatus() == IssueReport.IssueStatus.SUBMITTED).count();
        long underReview = reports.stream().filter(r -> r.getStatus() == IssueReport.IssueStatus.UNDER_REVIEW).count();
        long validated = reports.stream().filter(r -> r.getStatus() == IssueReport.IssueStatus.VERIFIED).count();
        long assigned = reports.stream().filter(r -> r.getStatus() == IssueReport.IssueStatus.ASSIGNED).count();
        long inProgress = reports.stream().filter(r -> r.getStatus() == IssueReport.IssueStatus.IN_PROGRESS).count();
        long resolved = reports.stream().filter(r -> r.getStatus() == IssueReport.IssueStatus.RESOLVED).count();
        long communityVerified = reports.stream().filter(r -> r.getStatus() == IssueReport.IssueStatus.COMMUNITY_VERIFIED).count();
        long rejected = reports.stream().filter(r -> r.getStatus() == IssueReport.IssueStatus.REJECTED).count();
        long emergency = reports.stream().filter(r -> Boolean.TRUE.equals(r.getIsEmergency())).count();
        long unassigned = reports.stream().filter(r -> r.getRoutingStatus() == IssueReport.RoutingStatus.UNASSIGNED).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReports", total);
        stats.put("submittedReports", submitted);
        stats.put("underReviewReports", underReview);
        stats.put("validatedReports", validated);
        stats.put("assignedReports", assigned);
        stats.put("inProgressReports", inProgress);
        stats.put("resolvedReports", resolved);
        stats.put("communityVerifiedReports", communityVerified);
        stats.put("rejectedReports", rejected);
        stats.put("emergencyReports", emergency);
        stats.put("unassignedReports", unassigned);
        return stats;
    }
}
