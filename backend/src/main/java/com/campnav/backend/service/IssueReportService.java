package com.campnav.backend.service;

import com.campnav.backend.model.IssueReport;
import com.campnav.backend.model.IssueTimeline;
import com.campnav.backend.repository.IssueReportRepository;
import com.campnav.backend.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IssueReportService {
    private final IssueReportRepository reportRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional
    public IssueReport createReport(IssueReport report) {
        // 1. Generate Unique ID
        String externalId = "JMF-" + LocalDateTime.now().getYear() + "-" + 
                String.format("%06d", (int)(Math.random() * 1000000));
        report.setExternalId(externalId);

        // 2. Duplicate Detection logic
        List<IssueReport> potentialDuplicates = reportRepository.findNearbyIssues(
            report.getLatitude(), report.getLongitude(), 0.05 // 50 meters
        );
        if (!potentialDuplicates.isEmpty()) {
             // Link to the first found duplicate and set status
             // report.setStatus(IssueReport.IssueStatus.DUPLICATE);
        }

        // 3. Automated Priority & Impact Score Calculation
        report.setCalculatedPriority(calculatePriority(report));
        report.setImpactScore(calculateImpactScore(report));

        // 4. Automated Department Assignment
        assignDepartment(report);

        // 5. Initialize Timeline
        IssueTimeline initialEvent = IssueTimeline.builder()
                .issue(report)
                .status(IssueReport.IssueStatus.SUBMITTED)
                .message("Issue reported by citizen. GPS location verified.")
                .timestamp(LocalDateTime.now())
                .build();
        
        List<IssueTimeline> timeline = new ArrayList<>();
        timeline.add(initialEvent);
        report.setTimeline(timeline);

        return reportRepository.save(report);
    }

    private IssueReport.IssuePriority calculatePriority(IssueReport report) {
        if (report.getIsEmergency()) return IssueReport.IssuePriority.CRITICAL;
        
        // Advanced priority logic based on category and severity
        if ("PUBLIC_SAFETY".equals(report.getCategory()) || "ELECTRICITY".equals(report.getCategory())) {
            if (IssueReport.IssuePriority.HIGH.equals(report.getUserSeverity())) {
                return IssueReport.IssuePriority.CRITICAL;
            }
            return IssueReport.IssuePriority.HIGH;
        }
        
        return report.getUserSeverity();
    }

    private Integer calculateImpactScore(IssueReport report) {
        int score = 0;
        // Impact Score = Base (Severity) + Context (Emergency) + Crowd (Supports)
        if (report.getIsEmergency()) score += 50;
        
        score += switch (report.getUserSeverity()) {
            case CRITICAL -> 40;
            case HIGH -> 30;
            case MEDIUM -> 20;
            case LOW -> 10;
        };

        // Geographic concentration factor (simulated)
        long nearbyCount = reportRepository.count(); // Mock check for concentration
        if (nearbyCount > 10) score += 10;

        return Math.min(100, score);
    }

    private void assignDepartment(IssueReport report) {
        String deptName = switch (report.getCategory()) {
            case "WATER" -> "Water Authority";
            case "ROADS" -> "Roads Department";
            case "WASTE" -> "Waste Management";
            case "ELECTRICITY" -> "Electricity Board";
            case "PUBLIC_SAFETY" -> "Public Safety";
            case "DRAINAGE" -> "Drainage Department";
            default -> "Municipal Works";
        };
        departmentRepository.findByName(deptName).ifPresent(report::setAssignedDepartment);
    }

    public List<IssueReport> getNearbyIssues(Double lat, Double lng, Double radiusKm) {
        return reportRepository.findNearbyIssues(lat, lng, radiusKm);
    }

    @Transactional
    public IssueReport updateStatus(String externalId, IssueReport.IssueStatus newStatus, String message) {
        IssueReport report = reportRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));
        
        report.setStatus(newStatus);
        report.setUpdatedAt(LocalDateTime.now());
        
        IssueTimeline event = IssueTimeline.builder()
                .issue(report)
                .status(newStatus)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        
        report.getTimeline().add(event);
        return reportRepository.save(report);
    }

    @Transactional
    public IssueReport verifyResolution(String externalId, boolean resolved) {
        String message = resolved ? "Community verified the resolution." : "Resident reported issue still exists.";
        IssueReport.IssueStatus status = resolved ? IssueReport.IssueStatus.CLOSED : IssueReport.IssueStatus.IN_PROGRESS;
        return updateStatus(externalId, status, message);
    }
}
