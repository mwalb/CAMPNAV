package com.campnav.backend.controller;

import com.campnav.backend.dto.StudentLeaderDTO;
import com.campnav.backend.model.EmailNotification;
import com.campnav.backend.model.StudentLeaderAssignment;
import com.campnav.backend.service.EmailNotificationService;
import com.campnav.backend.service.StudentLeaderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminStudentLeaderController {

    private final StudentLeaderService leaderService;
    private final EmailNotificationService emailService;

    @GetMapping("/leaders")
    public ResponseEntity<List<StudentLeaderDTO>> getAllLeaders() {
        return ResponseEntity.ok(leaderService.getAllAssignments());
    }

    @PostMapping("/leaders/appoint")
    public ResponseEntity<StudentLeaderDTO> appointLeader(
            @RequestBody AppointLeaderRequest request,
            Authentication authentication) {
        String adminUsername = getUsername(authentication);
        if (request.getUniversityId() == null) {
            throw new IllegalArgumentException("University ID is required");
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty() || !request.getEmail().contains("@")) {
            throw new IllegalArgumentException("Valid email address is required");
        }
        return ResponseEntity.ok(leaderService.appointStudentLeader(
                request.getUniversityId(),
                request.getFullName(),
                request.getEmail(),
                request.getLeaderUsername(),
                adminUsername
        ));
    }

    @PostMapping("/leaders/{id}/status")
    public ResponseEntity<StudentLeaderDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest request,
            Authentication authentication) {
        String adminUsername = getUsername(authentication);
        return ResponseEntity.ok(leaderService.updateAssignmentStatus(
                id,
                StudentLeaderAssignment.AssignmentStatus.valueOf(request.getStatus()),
                request.getReason(),
                adminUsername
        ));
    }

    @DeleteMapping("/leaders/{id}")
    public ResponseEntity<java.util.Map<String, String>> deleteLeader(
            @PathVariable Long id,
            Authentication authentication) {
        String adminUsername = getUsername(authentication);
        leaderService.deleteLeaderAssignment(id, adminUsername);
        return ResponseEntity.ok(java.util.Map.of("message", "Student Leader account deleted successfully"));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<EmailNotification>> getAllNotifications() {
        return ResponseEntity.ok(emailService.getAllNotifications());
    }

    @PostMapping("/notifications/{id}/retry")
    public ResponseEntity<Boolean> retryNotification(@PathVariable Long id) {
        return ResponseEntity.ok(emailService.retryNotification(id));
    }

    private String getUsername(Authentication authentication) {
        return authentication != null && authentication.getName() != null ? authentication.getName() : "admin";
    }

    @Data
    public static class AppointLeaderRequest {
        private Long universityId;
        private String fullName;
        private String email;
        private String leaderUsername;
    }

    @Data
    public static class StatusRequest {
        private String status;
        private String reason;
    }
}
