package com.campnav.backend.service;

import com.campnav.backend.model.*;
import com.campnav.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final CommunityContributionRepository contributionRepository;
    private final CommunityReviewRepository reviewRepository;
    private final AdminActivityLogRepository activityLogRepository;
    private final CampusLocationRepository locationRepository;

    public Map<String, Object> getDashboardStats() {
        List<CommunityContribution> allContribs = contributionRepository.findAll();
        List<CommunityReview> allReviews = reviewRepository.findAll();

        long pending = allContribs.stream().filter(c -> c.getStatus() == CommunityContribution.ContributionStatus.PENDING).count();
        long approved = allContribs.stream().filter(c -> c.getStatus() == CommunityContribution.ContributionStatus.APPROVED).count();
        long rejected = allContribs.stream().filter(c -> c.getStatus() == CommunityContribution.ContributionStatus.REJECTED).count();
        long universityArea = allContribs.stream().filter(c -> "UNIVERSITY_AREA".equalsIgnoreCase(c.getAreaType())).count();
        long commercialArea = allContribs.stream().filter(c -> "COMMERCIAL_AREA".equalsIgnoreCase(c.getAreaType())).count();
        long paymentPending = allContribs.stream().filter(c -> c.getPaymentStatus() == CommunityContribution.PaymentStatus.PAYMENT_PENDING).count();
        long paymentPaid = allContribs.stream().filter(c -> c.getPaymentStatus() == CommunityContribution.PaymentStatus.PAYMENT_PAID).count();
        long totalReviews = allReviews.size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingContributions", pending);
        stats.put("approvedContributions", approved);
        stats.put("rejectedContributions", rejected);
        stats.put("universityAreaSubmissions", universityArea);
        stats.put("commercialAreaSubmissions", commercialArea);
        stats.put("pendingCommercialPayments", paymentPending);
        stats.put("completedCommercialPayments", paymentPaid);
        stats.put("totalReviews", totalReviews);
        return stats;
    }

    @Transactional
    public CommunityContribution approveContribution(Long id, String adminUsername) {
        CommunityContribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contribution not found"));

        if (contribution.getStatus() == CommunityContribution.ContributionStatus.APPROVED) {
            return contribution;
        }

        contribution.setStatus(CommunityContribution.ContributionStatus.APPROVED);
        if (contribution.getPaymentStatus() == CommunityContribution.PaymentStatus.PAYMENT_PENDING) {
            contribution.setPaymentStatus(CommunityContribution.PaymentStatus.PAYMENT_PAID);
        }
        contribution.setReviewedAt(LocalDateTime.now());
        contribution.setReviewedBy(adminUsername);
        contribution.setUpdatedAt(LocalDateTime.now());
        CommunityContribution saved = contributionRepository.save(contribution);

        // Promote to official campus location table
        CampusLocation newLocation = CampusLocation.builder()
                .externalId("CONTRIB-" + saved.getId())
                .name(saved.getLocationName())
                .officialName(saved.getLocationName())
                .description(saved.getDescription())
                .university(saved.getUniversity())
                .category(saved.getCategory())
                .latitude(saved.getLatitude())
                .longitude(saved.getLongitude())
                .verificationStatus("VERIFIED")
                .source("COMMUNITY_CONTRIBUTION")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        locationRepository.save(newLocation);

        // Log activity
        AdminActivityLog log = AdminActivityLog.builder()
                .action("LOCATION_APPROVED")
                .actorUsername(adminUsername)
                .description("Approved contribution " + saved.getReference() + " (" + saved.getLocationName() + ") and promoted to official map location")
                .relatedEntityId(saved.getReference())
                .build();
        activityLogRepository.save(log);

        return saved;
    }

    @Transactional
    public CommunityContribution rejectContribution(Long id, String reason, String adminUsername) {
        CommunityContribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contribution not found"));

        contribution.setStatus(CommunityContribution.ContributionStatus.REJECTED);
        contribution.setRejectionReason(reason != null ? reason : "No reason provided");
        contribution.setReviewedAt(LocalDateTime.now());
        contribution.setReviewedBy(adminUsername);
        contribution.setUpdatedAt(LocalDateTime.now());
        CommunityContribution saved = contributionRepository.save(contribution);

        // Log activity
        AdminActivityLog log = AdminActivityLog.builder()
                .action("LOCATION_REJECTED")
                .actorUsername(adminUsername)
                .description("Rejected contribution " + saved.getReference() + ". Reason: " + saved.getRejectionReason())
                .relatedEntityId(saved.getReference())
                .build();
        activityLogRepository.save(log);

        return saved;
    }

    public List<AdminActivityLog> getAllLogs() {
        return activityLogRepository.findAllByOrderByTimestampDesc();
    }
}
