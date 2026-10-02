package com.campnav.backend.controller;

import com.campnav.backend.model.UserFeedback;
import com.campnav.backend.service.UserFeedbackService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserFeedbackController {

    private final UserFeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<UserFeedback> submitFeedback(@RequestBody FeedbackRequest request) {
        UserFeedback created = feedbackService.submitFeedback(
                request.getFeedbackText(),
                request.getContributorName(),
                request.getContributorContact()
        );
        return ResponseEntity.ok(created);
    }

    @GetMapping("/approved")
    public ResponseEntity<List<UserFeedback>> getApprovedFeedback() {
        return ResponseEntity.ok(feedbackService.getApprovedFeedback());
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<UserFeedback>> getAllFeedback(Authentication authentication) {
        return ResponseEntity.ok(feedbackService.getAllFeedback());
    }

    @PostMapping("/admin/{id}/approve")
    public ResponseEntity<UserFeedback> approveFeedback(
            @PathVariable Long id,
            Authentication authentication) {
        String adminUsername = authentication != null && authentication.getName() != null 
                ? authentication.getName() 
                : "admin";
        UserFeedback approved = feedbackService.approveFeedback(id, adminUsername);
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/admin/{id}/reject")
    public ResponseEntity<UserFeedback> rejectFeedback(
            @PathVariable Long id,
            @RequestBody AdminController.RejectRequest request,
            Authentication authentication) {
        String adminUsername = authentication != null && authentication.getName() != null 
                ? authentication.getName() 
                : "admin";
        UserFeedback rejected = feedbackService.rejectFeedback(id, request.getReason(), adminUsername);
        return ResponseEntity.ok(rejected);
    }

    @Data
    public static class FeedbackRequest {
        private String feedbackText;
        private String contributorName;
        private String contributorContact;
    }
}
