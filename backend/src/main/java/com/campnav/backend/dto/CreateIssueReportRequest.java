package com.campnav.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateIssueReportRequest {
    private Long universityId;
    private String campusName;

    @NotBlank(message = "Category is required")
    private String category;

    private String subCategory;

    @NotBlank(message = "Description is required")
    private String description;

    private String urgency; // LOW, NORMAL, HIGH, EMERGENCY
    private String userSeverity; // LOW, MEDIUM, HIGH, CRITICAL

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private String address;
    private String street;
    private String ward;
    private String district;
    private String region;
    private String nearbyLandmark;

    private Double reporterLatitude;
    private Double reporterLongitude;
    private Float locationAccuracy;

    private List<String> evidenceUrls;
    private String locationSource; // CURRENT_LOCATION, MAP_PICKER, MANUAL

    @NotBlank(message = "Reporter name is required")
    private String reporterName;

    private String reporterEmail;
    private String reporterPhone;
    private Boolean isEmergency;
}
