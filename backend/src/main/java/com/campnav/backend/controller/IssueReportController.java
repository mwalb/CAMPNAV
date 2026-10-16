package com.campnav.backend.controller;

import com.campnav.backend.dto.CreateIssueReportRequest;
import com.campnav.backend.dto.IssueReportResponse;
import com.campnav.backend.model.IssueReport;
import com.campnav.backend.service.IssueReportService;
import jakarta.validation.Valid;
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
    public ResponseEntity<IssueReportResponse> createReport(@Valid @RequestBody CreateIssueReportRequest request) {
        return ResponseEntity.ok(reportService.createReport(request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @GetMapping("/community/reports")
    public ResponseEntity<List<IssueReportResponse>> getAllReports(@RequestParam(required = false) Long universityId) {
        if (universityId != null) {
            return ResponseEntity.ok(reportService.getReportsByUniversity(universityId));
        }
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/universities/{universityId}/reports")
    public ResponseEntity<List<IssueReportResponse>> getReportsByUniversity(@PathVariable Long universityId) {
        return ResponseEntity.ok(reportService.getReportsByUniversity(universityId));
    }

    @GetMapping("/community/reports/nearby")
    public ResponseEntity<List<IssueReportResponse>> getNearbyReports(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "2.0") Double radius) {
        return ResponseEntity.ok(reportService.getNearbyIssues(lat, lng, radius));
    }

    @GetMapping("/community/reports/{externalId}")
    public ResponseEntity<IssueReportResponse> getReportDetails(@PathVariable String externalId) {
        return ResponseEntity.ok(reportService.getReportByExternalId(externalId));
    }

    @GetMapping("/community/reports/track/{externalId}")
    public ResponseEntity<IssueReportResponse> trackReport(@PathVariable String externalId) {
        return ResponseEntity.ok(reportService.getReportByExternalId(externalId));
    }

    @PostMapping("/community/reports/{externalId}/confirm")
    public ResponseEntity<IssueReportResponse> confirmIssue(@PathVariable String externalId) {
        return ResponseEntity.ok(reportService.confirmIssue(externalId));
    }

    @PostMapping("/community/reports/{externalId}/verify")
    public ResponseEntity<IssueReportResponse> verifyResolution(
            @PathVariable String externalId,
            @RequestParam boolean resolved,
            Authentication authentication) {
        String username = authentication != null && authentication.getName() != null ? authentication.getName() : "citizen";
        return ResponseEntity.ok(reportService.verifyResolution(externalId, resolved, username));
    }

    // --- Admin Issue Management Endpoints ---
    @GetMapping("/admin/issues")
    public ResponseEntity<List<IssueReportResponse>> getAdminAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/admin/issues/stats")
    public ResponseEntity<Map<String, Object>> getAdminIssueStats() {
        return ResponseEntity.ok(reportService.getAdminStats());
    }

    @PostMapping("/admin/issues/{externalId}/status")
    public ResponseEntity<IssueReportResponse> updateStatus(
            @PathVariable String externalId,
            @RequestBody StatusUpdateRequest request,
            Authentication authentication) {
        String adminUsername = authentication != null && authentication.getName() != null ? authentication.getName() : "admin";
        IssueReportResponse updated = reportService.updateStatus(
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
    public ResponseEntity<IssueReportResponse> assignIssue(
            @PathVariable String externalId,
            @RequestBody AssignRequest request,
            Authentication authentication) {
        String adminUsername = authentication != null && authentication.getName() != null ? authentication.getName() : "admin";
        IssueReportResponse updated = reportService.assignIssue(externalId, request.getDepartmentId(), adminUsername);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/admin/issues/{externalId}/resolve")
    public ResponseEntity<IssueReportResponse> resolveIssue(
            @PathVariable String externalId,
            @RequestBody ResolveRequest request,
            Authentication authentication) {
        String adminUsername = authentication != null && authentication.getName() != null ? authentication.getName() : "admin";
        IssueReportResponse updated = reportService.resolveIssue(externalId, request.getResolutionDescription(), adminUsername);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/admin/issues/{externalId}/resend-notification")
    public ResponseEntity<com.campnav.backend.model.EmailNotification> resendLeaderNotification(
            @PathVariable String externalId,
            Authentication authentication) {
        String adminUsername = authentication != null && authentication.getName() != null ? authentication.getName() : "admin";
        com.campnav.backend.model.EmailNotification notification = reportService.resendLeaderNotificationByAdmin(externalId, adminUsername);
        return ResponseEntity.ok(notification);
    }

    @DeleteMapping("/admin/issues/{externalId}")
    public ResponseEntity<Map<String, String>> deleteAdminReport(
            @PathVariable String externalId,
            Authentication authentication) {
        String adminUsername = authentication != null && authentication.getName() != null ? authentication.getName() : "admin";
        reportService.deleteReport(externalId, adminUsername);
        return ResponseEntity.ok(Map.of("message", "Issue report deleted successfully"));
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
