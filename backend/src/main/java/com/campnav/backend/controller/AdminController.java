package com.campnav.backend.controller;

import com.campnav.backend.model.AdminActivityLog;
import com.campnav.backend.model.CommunityContribution;
import com.campnav.backend.service.AdminService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @PostMapping("/contributions/{id}/approve")
    public ResponseEntity<CommunityContribution> approveContribution(
            @PathVariable Long id,
            Authentication authentication) {
        String adminUsername = authentication != null && authentication.getName() != null 
                ? authentication.getName() 
                : "admin";
        CommunityContribution updated = adminService.approveContribution(id, adminUsername);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/contributions/{id}/reject")
    public ResponseEntity<CommunityContribution> rejectContribution(
            @PathVariable Long id,
            @RequestBody RejectRequest request,
            Authentication authentication) {
        String adminUsername = authentication != null && authentication.getName() != null 
                ? authentication.getName() 
                : "admin";
        CommunityContribution updated = adminService.rejectContribution(id, request.getReason(), adminUsername);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/logs")
    public ResponseEntity<List<AdminActivityLog>> getAllLogs() {
        return ResponseEntity.ok(adminService.getAllLogs());
    }

    @Data
    public static class RejectRequest {
        private String reason;
    }
}
