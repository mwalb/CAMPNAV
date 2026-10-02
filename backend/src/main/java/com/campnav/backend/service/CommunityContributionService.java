package com.campnav.backend.service;

import com.campnav.backend.model.*;
import com.campnav.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityContributionService {

    private final CommunityContributionRepository contributionRepository;
    private final UniversityRepository universityRepository;
    private final CategoryRepository categoryRepository;
    private final CampusLocationRepository locationRepository;
    private final AdminActivityLogRepository activityLogRepository;

    @Transactional
    public CommunityContribution submitContribution(
            Long universityId,
            String locationName,
            String areaType,
            Long categoryId,
            String customCategory,
            String description,
            Double latitude,
            Double longitude,
            String contributorName,
            String contributorContact) {

        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new RuntimeException("University not found"));

        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId).orElse(null);
        } else if (customCategory != null && !customCategory.trim().isEmpty()) {
            String customName = customCategory.trim();
            category = categoryRepository.findByUniversityIdAndNameIgnoreCase(universityId, customName)
                    .orElseGet(() -> {
                        Category newCat = Category.builder()
                                .university(university)
                                .name(customName)
                                .slug(customName.toLowerCase().replaceAll("[^a-z0-9]", "-"))
                                .description("Custom category submitted by user")
                                .iconName("place")
                                .isActive(true)
                                .sortOrder(99)
                                .build();
                        return categoryRepository.save(newCat);
                    });
        }

        // Duplicate protection check
        boolean exists = contributionRepository.existsByUniversityIdAndLocationNameIgnoreCaseAndLatitudeAndLongitude(
                universityId, locationName, latitude, longitude);
        if (exists) {
            throw new RuntimeException("A similar location contribution already exists for this university and coordinates.");
        }

        String reference = "CAMPNAV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        CommunityContribution.PaymentStatus paymentStatus = "COMMERCIAL_AREA".equalsIgnoreCase(areaType)
                ? CommunityContribution.PaymentStatus.PAYMENT_PENDING
                : CommunityContribution.PaymentStatus.PAYMENT_NOT_REQUIRED;

        CommunityContribution contribution = CommunityContribution.builder()
                .reference(reference)
                .university(university)
                .locationName(locationName)
                .areaType(areaType != null ? areaType.toUpperCase() : "UNIVERSITY_AREA")
                .category(category)
                .description(description)
                .latitude(latitude)
                .longitude(longitude)
                .contributorName(contributorName)
                .contributorContact(contributorContact)
                .status(CommunityContribution.ContributionStatus.PENDING)
                .paymentStatus(paymentStatus)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CommunityContribution saved = contributionRepository.save(contribution);

        // Log activity
        AdminActivityLog log = AdminActivityLog.builder()
                .action("LOCATION_SUBMITTED")
                .actorUsername(contributorName + " (" + contributorContact + ")")
                .description("New location submitted: " + locationName + " under " + university.getName() + " [" + areaType + "]")
                .relatedEntityId(reference)
                .build();
        activityLogRepository.save(log);

        return saved;
    }

    public Optional<CommunityContribution> getByReference(String reference) {
        return contributionRepository.findByReference(reference);
    }

    public List<CommunityContribution> getAllContributions() {
        return contributionRepository.findAll();
    }

    public List<CommunityContribution> getContributionsByStatus(CommunityContribution.ContributionStatus status) {
        return contributionRepository.findByStatus(status);
    }
}
