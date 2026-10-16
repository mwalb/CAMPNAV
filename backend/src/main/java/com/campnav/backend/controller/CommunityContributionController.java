package com.campnav.backend.controller;

import com.campnav.backend.model.CommunityContribution;
import com.campnav.backend.service.CommunityContributionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommunityContributionController {

    private final CommunityContributionService contributionService;

    @PostMapping("/contributions")
    public ResponseEntity<CommunityContribution> submitContribution(@Valid @RequestBody ContributionRequest request) {
        CommunityContribution contribution = contributionService.submitContribution(
                request.getUniversityId(),
                request.getLocationName(),
                request.getAreaType(),
                request.getCategoryId(),
                request.getCustomCategory(),
                request.getDescription(),
                request.getLatitude(),
                request.getLongitude(),
                request.getContributorName(),
                request.getContributorContact()
        );
        return ResponseEntity.ok(contribution);
    }

    @GetMapping("/contributions/{reference}")
    public ResponseEntity<CommunityContribution> getContributionByReference(@PathVariable String reference) {
        return contributionService.getByReference(reference)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/contributions")
    public ResponseEntity<List<CommunityContribution>> getAllAdminContributions(
            @RequestParam(required = false) CommunityContribution.ContributionStatus status) {
        if (status != null) {
            return ResponseEntity.ok(contributionService.getContributionsByStatus(status));
        }
        return ResponseEntity.ok(contributionService.getAllContributions());
    }

    @Data
    public static class ContributionRequest {
        @NotNull(message = "University ID is required")
        private Long universityId;
        @NotBlank(message = "Location name is required")
        private String locationName;
        @NotBlank(message = "Area type is required")
        private String areaType;
        private Long categoryId;
        private String customCategory;
        @NotBlank(message = "Description is required")
        private String description;
        @NotNull(message = "Latitude is required")
        private Double latitude;
        @NotNull(message = "Longitude is required")
        private Double longitude;
        @NotBlank(message = "Contributor name is required")
        private String contributorName;
        private String contributorContact;
    }
}
