package com.campnav.backend.service;

import com.campnav.backend.model.*;
import com.campnav.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityReviewService {

    private final CommunityReviewRepository reviewRepository;
    private final UniversityRepository universityRepository;
    private final CampusLocationRepository locationRepository;
    private final AdminActivityLogRepository activityLogRepository;

    @Transactional
    public CommunityReview addReview(
            Long universityId,
            Long locationId,
            Integer rating,
            String comment,
            String contributorName,
            String contributorContact) {

        if (rating == null || rating < 1 || rating > 5) {
            throw new RuntimeException("Rating must be between 1 and 5.");
        }

        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new RuntimeException("University not found"));

        CampusLocation location = locationId != null ? locationRepository.findById(locationId).orElse(null) : null;

        CommunityReview review = CommunityReview.builder()
                .university(university)
                .location(location)
                .rating(rating)
                .comment(comment)
                .contributorName(contributorName != null ? contributorName : "Anonymous")
                .contributorContact(contributorContact)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        CommunityReview saved = reviewRepository.save(review);

        // Log activity
        AdminActivityLog log = AdminActivityLog.builder()
                .action("REVIEW_SUBMITTED")
                .actorUsername(saved.getContributorName())
                .description("New review (" + rating + "★) submitted for university ID " + universityId)
                .relatedEntityId(String.valueOf(saved.getId()))
                .build();
        activityLogRepository.save(log);

        return saved;
    }

    public UserFeedback approveReview(Long id, String adminUsername) {
        return null;
    }

    @Transactional
    public CommunityReview approveReviewAction(Long id, String adminUsername) {
        CommunityReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
        review.setStatus("APPROVED");
        CommunityReview saved = reviewRepository.save(review);

        AdminActivityLog log = AdminActivityLog.builder()
                .action("APPROVE_REVIEW")
                .actorUsername(adminUsername)
                .description("Approved community review ID: " + id)
                .relatedEntityId("REVIEW-" + id)
                .build();
        activityLogRepository.save(log);

        return saved;
    }

    @Transactional
    public CommunityReview rejectReview(Long id, String reason, String adminUsername) {
        CommunityReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
        review.setStatus("REJECTED");
        CommunityReview saved = reviewRepository.save(review);

        AdminActivityLog log = AdminActivityLog.builder()
                .action("REJECT_REVIEW")
                .actorUsername(adminUsername)
                .description("Rejected community review ID: " + id + ", Reason: " + reason)
                .relatedEntityId("REVIEW-" + id)
                .build();
        activityLogRepository.save(log);

        return saved;
    }

    public List<CommunityReview> getReviewsByUniversity(Long universityId) {
        return reviewRepository.findByUniversityIdAndStatus(universityId, "APPROVED");
    }

    public List<CommunityReview> getReviewsByLocation(Long locationId) {
        return reviewRepository.findByLocationId(locationId);
    }

    public List<CommunityReview> getAllReviews() {
        return reviewRepository.findAll();
    }
}
