package com.campnav.backend.controller;

import com.campnav.backend.dto.IssueReportResponse;
import com.campnav.backend.model.IssueReport;
import com.campnav.backend.model.UniversityOfficeReferral;
import com.campnav.backend.service.IssueReportService;
import com.campnav.backend.service.StudentLeaderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leader")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentLeaderController {

    private final StudentLeaderService leaderService;
    private final IssueReportService reportService;

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview(Authentication authentication) {
        String username = getUsername(authentication);
        return ResponseEntity.ok(leaderService.getLeaderOverviewStats(username));
    }

    @GetMapping("/issues")
    public ResponseEntity<List<IssueReportResponse>> getAssignedIssues(Authentication authentication) {
        String username = getUsername(authentication);
        List<Long> univIds = leaderService.getAuthorizedUniversityIdsForUser(username);
        if (univIds.isEmpty()) return ResponseEntity.ok(List.of());

        // Returns issues for all authorized universities
        List<IssueReportResponse> reports = reportService.getAllReportEntities().stream()
                .filter(r -> r.getUniversity() != null && univIds.contains(r.getUniversity().getId()))
                .map(reportService::mapToResponse)
                .toList();

        return ResponseEntity.ok(reports);
    }

    @PostMapping("/issues/{externalId}/acknowledge")
    public ResponseEntity<IssueReportResponse> acknowledgeReport(
            @PathVariable String externalId,
            Authentication authentication) {
        String username = getUsername(authentication);
        IssueReport report = reportService.getReportEntityByExternalId(externalId);
        checkUniversityAuthorization(username, report);
        IssueReport updated = reportService.acknowledgeReportByLeader(externalId, username);
        return ResponseEntity.ok(reportService.mapToResponse(updated));
    }

    @PostMapping("/issues/{externalId}/refer")
    public ResponseEntity<UniversityOfficeReferral> referToOffice(
            @PathVariable String externalId,
            @RequestBody ReferralRequest request,
            Authentication authentication) {
        String username = getUsername(authentication);
        IssueReport report = reportService.getReportEntityByExternalId(externalId);
        checkUniversityAuthorization(username, report);
        LocalDateTime followUp = request.getFollowUpDays() != null ? LocalDateTime.now().plusDays(request.getFollowUpDays()) : null;
        return ResponseEntity.ok(reportService.referReportToUniversityOffice(
                externalId,
                request.getOfficeName(),
                request.getOfficeContact(),
                request.getReferralChannel(),
                followUp,
                username
        ));
    }

    @PostMapping("/issues/{externalId}/resolve")
    public ResponseEntity<IssueReportResponse> resolveIssue(
            @PathVariable String externalId,
            @RequestBody ResolveRequest request,
            Authentication authentication) {
        String username = getUsername(authentication);
        IssueReport report = reportService.getReportEntityByExternalId(externalId);
        checkUniversityAuthorization(username, report);
        IssueReport updated = reportService.leaderResolveReport(
                externalId,
                request.getResolutionDescription(),
                request.getAfterImageUrl(),
                username
        );
        return ResponseEntity.ok(reportService.mapToResponse(updated));
    }

    @DeleteMapping("/issues/{externalId}")
    public ResponseEntity<Map<String, String>> deleteLeaderReport(
            @PathVariable String externalId,
            Authentication authentication) {
        String username = getUsername(authentication);
        IssueReport report = reportService.getReportEntityByExternalId(externalId);
        checkUniversityAuthorization(username, report);
        reportService.deleteReport(externalId, username);
        return ResponseEntity.ok(Map.of("message", "Issue report deleted successfully"));
    }

    private void checkUniversityAuthorization(String username, IssueReport report) {
        if (report.getUniversity() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Access denied: Unrouted report has no verified university assignment. Only Admin can manage unrouted reports."
            );
        }
        List<Long> authorizedUnivIds = leaderService.getAuthorizedUniversityIdsForUser(username);
        if (!authorizedUnivIds.contains(report.getUniversity().getId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Access denied: Student leader is not authorized for university " + report.getUniversity().getName()
            );
        }
    }

    private String getUsername(Authentication authentication) {
        return authentication != null && authentication.getName() != null ? authentication.getName() : "leader";
    }

    @Data
    public static class ReferralRequest {
        private String officeName;
        private String officeContact;
        private String referralChannel;
        private Integer followUpDays;
    }

    @Data
    public static class ResolveRequest {
        private String resolutionDescription;
        private String afterImageUrl;
    }
}
