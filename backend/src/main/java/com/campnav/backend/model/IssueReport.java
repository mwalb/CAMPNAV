package com.campnav.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "issue_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String externalId;

    @Column(nullable = false)
    private String category;

    private String subCategory;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String voiceTranscription;

    // University Scope & Location Intelligence
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "university_id")
    private University university;

    private String campusName;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String address;
    private String street;
    private String ward;
    private String district;
    private String region;
    private String nearbyLandmark;

    private Double reporterLatitude;
    private Double reporterLongitude;
    private Double distanceToIssue;
    private Float locationAccuracy;

    // Content
    @ElementCollection
    @CollectionTable(name = "issue_evidence", joinColumns = @JoinColumn(name = "issue_id"))
    @Column(name = "evidence_url")
    private List<String> evidenceUrls;

    private String videoUrl;
    private String voiceUrl;
    private String beforeImageUrl;
    private String afterImageUrl;
    private String locationSource; // CURRENT_LOCATION, MAP_PICKER, MANUAL

    @Column(columnDefinition = "TEXT")
    private String internalNotes;

    @Column(columnDefinition = "TEXT")
    private String publicResponse;

    // Status & Priority & Urgency
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private IssueStatus status = IssueStatus.SUBMITTED;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoutingStatus routingStatus = RoutingStatus.UNASSIGNED;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UrgencyLevel urgency = UrgencyLevel.NORMAL;

    @Enumerated(EnumType.STRING)
    private IssuePriority userSeverity;

    @Enumerated(EnumType.STRING)
    private IssuePriority calculatedPriority;

    @Builder.Default
    private Integer impactScore = 0;

    @Builder.Default
    private Boolean isEmergency = false;

    // Routing & Assignment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private CommunityUser reporter;

    private String reporterName;
    private String reporterEmail;
    private String reporterPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_leader_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private CommunityUser assignedLeader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_department_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Department assignedDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_officer_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private CommunityUser assignedOfficer;

    @Builder.Default
    private String notificationStatus = "PENDING";

    // Interaction
    @Builder.Default
    private Integer supportsCount = 0;

    @Builder.Default
    private Integer verificationCount = 0;

    // Audit
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<IssueTimeline> timeline;

    @JsonProperty("id")
    public String getIdForJson() {
        return this.externalId != null && !this.externalId.isBlank() ? this.externalId : (this.id != null ? this.id.toString() : "");
    }

    @JsonProperty("id")
    public void setIdFromJson(Object idVal) {
        if (idVal != null) {
            String str = idVal.toString().trim();
            if (!str.isEmpty()) {
                if (str.startsWith("CAM-")) {
                    this.externalId = str;
                } else {
                    try {
                        this.id = Long.parseLong(str);
                    } catch (NumberFormatException e) {
                        this.externalId = str;
                    }
                }
            }
        }
    }

    @JsonProperty("userPriority")
    public void setUserPriorityFromJson(Object priority) {
        if (priority != null) {
            try {
                this.userSeverity = IssuePriority.valueOf(priority.toString().trim().toUpperCase());
            } catch (Exception e) {
                this.userSeverity = IssuePriority.MEDIUM;
            }
        }
    }

    @JsonProperty("userPriority")
    public IssuePriority getUserPriorityForJson() {
        return this.userSeverity != null ? this.userSeverity : IssuePriority.MEDIUM;
    }

    @JsonProperty("universityId")
    public void setUniversityId(Long universityId) {
        if (universityId != null) {
            if (this.university == null) {
                this.university = new University();
            }
            this.university.setId(universityId);
        }
    }

    @JsonProperty("universityId")
    public Long getUniversityId() {
        return this.university != null ? this.university.getId() : null;
    }

    @JsonProperty("universityName")
    public String getUniversityName() {
        return this.university != null ? this.university.getName() : null;
    }

    @JsonProperty("issueLocation")
    public void setIssueLocation(Map<String, Object> location) {
        if (location != null) {
            if (location.get("latitude") != null) {
                this.latitude = ((Number) location.get("latitude")).doubleValue();
            }
            if (location.get("longitude") != null) {
                this.longitude = ((Number) location.get("longitude")).doubleValue();
            }
        }
    }

    @JsonProperty("issueLocation")
    public Map<String, Object> getIssueLocationMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("latitude", this.latitude != null ? this.latitude : 0.0);
        map.put("longitude", this.longitude != null ? this.longitude : 0.0);
        return map;
    }

    @JsonProperty("reporterLocation")
    public void setReporterLocation(Map<String, Object> location) {
        if (location != null) {
            if (location.get("latitude") != null) {
                this.reporterLatitude = ((Number) location.get("latitude")).doubleValue();
            }
            if (location.get("longitude") != null) {
                this.reporterLongitude = ((Number) location.get("longitude")).doubleValue();
            }
        }
    }

    @JsonProperty("reporterLocation")
    public Map<String, Object> getReporterLocationMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("latitude", this.reporterLatitude != null ? this.reporterLatitude : 0.0);
        map.put("longitude", this.reporterLongitude != null ? this.reporterLongitude : 0.0);
        return map;
    }

    @JsonProperty("evidenceUris")
    public void setEvidenceUris(List<String> uris) {
        if (uris != null) {
            this.evidenceUrls = uris;
        }
    }

    @JsonProperty("evidenceUris")
    public List<String> getEvidenceUrisList() {
        return this.evidenceUrls != null ? this.evidenceUrls : Collections.emptyList();
    }

    public enum IssueStatus {
        DRAFT, SUBMITTED, RECEIVED, UNDER_REVIEW, VERIFIED, ASSIGNED,
        IN_PROGRESS, WAITING_FOR_ACTION, RESOLVED, COMMUNITY_VERIFIED,
        CLOSED, REJECTED, DUPLICATE, REOPENED
    }

    public enum RoutingStatus {
        UNASSIGNED, ASSIGNED_TO_LEADER, LEADER_ACKNOWLEDGED, REFERRED_TO_UNIVERSITY,
        AWAITING_RESPONSE, ROUTING_FAILED, CLOSED
    }

    public enum UrgencyLevel {
        LOW, NORMAL, HIGH, EMERGENCY
    }

    public enum IssuePriority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
