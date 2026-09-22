package com.campnav.backend.controller;

import com.campnav.backend.model.IssueReport;
import com.campnav.backend.service.IssueReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IssueReportController {
    private final IssueReportService reportService;

    @PostMapping
    public ResponseEntity<IssueReport> createReport(@RequestBody IssueReport report) {
        return ResponseEntity.ok(reportService.createReport(report));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<IssueReport>> getNearbyReports(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "2.0") Double radius) {
        return ResponseEntity.ok(reportService.getNearbyIssues(lat, lng, radius));
    }

    @PostMapping("/{externalId}/verify")
    public ResponseEntity<IssueReport> verifyResolution(
            @PathVariable String externalId,
            @RequestParam boolean resolved) {
        return ResponseEntity.ok(reportService.verifyResolution(externalId, resolved));
    }

    @GetMapping("/{externalId}")
    public ResponseEntity<IssueReport> getReportDetails(@PathVariable String externalId) {
        // Implementation for single report fetch could be added here
        return ResponseEntity.ok(null); 
    }
}
