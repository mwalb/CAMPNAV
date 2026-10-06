package com.campnav.backend.controller;

import com.campnav.backend.model.IssueReport;
import com.campnav.backend.service.IssueReportService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IssueReportController {
    private final IssueReportService reportService;

    @PostMapping("/community/reports")
    public ResponseEntity<IssueReport> createReport(@RequestBody IssueReport report) {
        return ResponseEntity.ok(reportService.createReport(report));
    }

    @GetMapping("/community/reports")
    public ResponseEntity<List<IssueReport>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/community/reports/nearby")
    public ResponseEntity<List<IssueReport>> getNearbyReports(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "2.0") Double radius) {
        return ResponseEntity.ok(reportService.getNearbyIssues(lat, lng, radius));
    }

    @GetMapping("/community/reports/{externalId}")
    public ResponseEntity<IssueReport> getReportDetails(@PathVariable String externalId) {
        return ResponseEntity.ok(reportService.getReportByExternalId(externalId));
    }

    @GetMapping("/community/reports/track/{externalId}")
    public ResponseEntity<IssueReport> trackReport(@PathVariable String externalId) {
        return ResponseEntity.ok(reportService.getReportByExternalId(externalId));
    }

    @PostMapping("/community/reports/{externalId}/confirm")
    public ResponseEntity<IssueReport> confirmIssue(@PathVariable String externalId) {
        return ResponseEntity.ok(reportService.confirmIssue(externalId));
    }

    @PostMapping("/community/reports/{externalId}/verify")
    public ResponseEntity<IssueReport> verifyResolution(
            @PathVariable String externalId,
            @RequestParam boolean resolved,
            Authentication authentication) {
        String username = authentication != null && authentication.getName() != null ? authentication.getName() : "citizen";
        return ResponseEntity.ok(reportService.verifyResolution(externalId, resolved, username));
    }

    // --- Admin Issue Management Endpoints ---
    @GetMapping("/admin/issues")
    public ResponseEntity<List<IssueReport>> getAdminAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/admin/issues/stats")
    public ResponseEntity<Map<String, Object>> getAdminIssueStats() {
        return ResponseEntity.ok(reportService.getAdminStats());
    }

    @PostMapping("/admin/issues/{externalId}/status")
    public ResponseEntity<IssueReport> updateStatus(
            @PathVariable String externalId,
            @RequestBody StatusUpdateRequest request,
            Authentication authentication) {
        String adminUsername = authentication != null && authentication.getName() != null ? authentication.getName() : "admin";
        IssueReport updated = reportService.updateStatus(
                externalId,
                request.getStatus(),
                request.getMessage(),
                request.getInternalNotes(),
                request.getPublicResponse(),
                adminUsername
        );
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/admin/issues/{externalId}/assign")
    public ResponseEntity<IssueReport> assignIssue(
            @PathVariable String externalId,
            @RequestBody AssignRequest request,
            Authentication authentication) {
        String adminUsername = authentication != null && authentication.getName() != null ? authentication.getName() : "admin";
        IssueReport updated = reportService.assignIssue(externalId, request.getDepartmentId(), adminUsername);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/admin/issues/{externalId}/resolve")
    public ResponseEntity<IssueReport> resolveIssue(
            @PathVariable String externalId,
            @RequestBody ResolveRequest request,
            Authentication authentication) {
        String adminUsername = authentication != null && authentication.getName() != null ? authentication.getName() : "admin";
        IssueReport updated = reportService.resolveIssue(externalId, request.getResolutionDescription(), adminUsername);
        return ResponseEntity.ok(updated);
    }

    @Data
    public static class StatusUpdateRequest {
        private IssueReport.IssueStatus status;
        private String message;
        private String internalNotes;
        private String publicResponse;
    }

    @Data
    public static class AssignRequest {
        private Long departmentId;
    }

    @Data
    public static class ResolveRequest {
        private String resolutionDescription;
    }
}
