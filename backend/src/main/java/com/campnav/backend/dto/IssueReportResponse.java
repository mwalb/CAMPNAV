package com.campnav.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueReportResponse {
    private String id; // externalId
    private Long dbId;
    private Long universityId;
    private String universityName;
    private String campusName;
    private String category;
    private String subCategory;
    private String description;
    private String voiceTranscription;
    private String status;
    private String routingStatus;
    private String urgency;
    private String userSeverity;
    private String calculatedPriority;
    private Integer impactScore;
    private Double latitude;
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
    private Map<String, Object> issueLocation;
    private Map<String, Object> reporterLocation;
    private List<String> evidenceUrls;
    @JsonProperty("evidenceUris")
    private List<String> evidenceUris;
    private String locationSource;
    private String reporterName;
    private String reporterEmail;
    private String reporterPhone;
    private Integer supportsCount;
    private Integer verificationCount;
    private Boolean isEmergency;
    private String publicResponse;
    private String internalNotes;
    private String timestamp; // ISO-8601 date-time string
    private String lastUpdated; // ISO-8601 date-time string
    private String updatedAt; // ISO-8601 date-time string
    private List<IssueTimelineResponse> timeline;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssueTimelineResponse {
        private String timestamp; // ISO-8601 date-time string
        private String status;
        private String message;
        private String actorId;
    }
}

