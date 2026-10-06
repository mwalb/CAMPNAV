package com.campnav.backend.service;

import com.campnav.backend.model.AdminActivityLog;
import com.campnav.backend.model.IssueReport;
import com.campnav.backend.model.IssueTimeline;
import com.campnav.backend.repository.AdminActivityLogRepository;
import com.campnav.backend.repository.DepartmentRepository;
import com.campnav.backend.repository.IssueReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IssueReportService {
    private final IssueReportRepository reportRepository;
    private final DepartmentRepository departmentRepository;
    private final AdminActivityLogRepository activityLogRepository;

    @Transactional
    public IssueReport createReport(IssueReport report) {
        // 1. Generate Unique ID in format CAM-YYYYMMDD-XXXXX
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String externalId = "CAM-" + dateStr + "-" + String.format("%05d", (int)(Math.random() * 90000) + 10000);
        report.setExternalId(externalId);

        if (report.getStatus() == null) {
            report.setStatus(IssueReport.IssueStatus.SUBMITTED);
        }

        // 2. Duplicate Detection logic (50 meters)
        if (report.getLatitude() != null && report.getLongitude() != null) {
            List<IssueReport> potentialDuplicates = reportRepository.findNearbyIssues(
                report.getLatitude(), report.getLongitude(), 0.05
            );
            if (!potentialDuplicates.isEmpty()) {
                // Keep active/submitted
            }
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

        // 5. Initialize Timeline
        IssueTimeline initialEvent = IssueTimeline.builder()
                .issue(report)
                .status(report.getStatus())
                .message("Report successfully submitted and registered with ID " + externalId)
                .timestamp(LocalDateTime.now())
                .build();
        
        List<IssueTimeline> timeline = new ArrayList<>();
        timeline.add(initialEvent);
        report.setTimeline(timeline);
        report.setTimestamp(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        IssueReport saved = reportRepository.save(report);

        // Log activity
        AdminActivityLog log = AdminActivityLog.builder()
                .action("ISSUE_CREATED")
                .actorUsername(report.getReporter() != null ? report.getReporter().getUsername() : "citizen")
                .description("New issue reported: " + saved.getExternalId() + " (" + saved.getCategory() + ")")
                .relatedEntityId(saved.getExternalId())
                .build();
        activityLogRepository.save(log);

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

        // Audit log
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
        return stats;
    }
}
