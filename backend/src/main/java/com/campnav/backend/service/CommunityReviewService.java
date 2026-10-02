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
                .status("APPROVED")
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

    public List<CommunityReview> getReviewsByUniversity(Long universityId) {
        return reviewRepository.findByUniversityId(universityId);
    }

    public List<CommunityReview> getReviewsByLocation(Long locationId) {
        return reviewRepository.findByLocationId(locationId);
    }

    public List<CommunityReview> getAllReviews() {
        return reviewRepository.findAll();
    }
}
