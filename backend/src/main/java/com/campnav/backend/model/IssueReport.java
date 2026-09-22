package com.campnav.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "issue_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    // Location Intelligence
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

    // Status & Priority
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private IssueStatus status = IssueStatus.SUBMITTED;

    @Enumerated(EnumType.STRING)
    private IssuePriority userSeverity;

    @Enumerated(EnumType.STRING)
    private IssuePriority calculatedPriority;

    @Builder.Default
    private Integer impactScore = 0;

    @Builder.Default
    private Boolean isEmergency = false;

    // Assignment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private CommunityUser reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_department_id")
    private Department assignedDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_officer_id")
    private CommunityUser assignedOfficer;

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

    public enum IssueStatus {
        DRAFT, SUBMITTED, RECEIVED, UNDER_REVIEW, VERIFIED, ASSIGNED,
        IN_PROGRESS, WAITING_FOR_ACTION, RESOLVED, COMMUNITY_VERIFIED,
        CLOSED, REJECTED, DUPLICATE, REOPENED
    }

    public enum IssuePriority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
