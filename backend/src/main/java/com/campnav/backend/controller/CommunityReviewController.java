package com.campnav.backend.controller;

import com.campnav.backend.model.CommunityReview;
import com.campnav.backend.service.CommunityReviewService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommunityReviewController {

    private final CommunityReviewService reviewService;

    @GetMapping("/universities/{id}/reviews")
    public ResponseEntity<List<CommunityReview>> getReviewsByUniversity(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewsByUniversity(id));
    }

    @PostMapping("/reviews")
    public ResponseEntity<CommunityReview> addReview(@RequestBody ReviewRequest request) {
        CommunityReview review = reviewService.addReview(
                request.getUniversityId(),
                request.getLocationId(),
                request.getRating(),
                request.getComment(),
                request.getContributorName(),
                request.getContributorContact()
        );
        return ResponseEntity.ok(review);
    }

    @GetMapping("/admin/reviews")
    public ResponseEntity<List<CommunityReview>> getAllAdminReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @Data
    public static class ReviewRequest {
        private Long universityId;
        private Long locationId;
        private Integer rating;
        private String comment;
        private String contributorName;
        private String contributorContact;
    }
}
