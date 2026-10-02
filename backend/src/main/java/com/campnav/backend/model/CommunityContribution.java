package com.campnav.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "community_contribution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityContribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @Column(nullable = false)
    private String locationName;

    @Column(nullable = false)
    private String areaType; // 'UNIVERSITY_AREA' or 'COMMERCIAL_AREA'

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private String contributorName;

    @Column(nullable = false)
    private String contributorContact;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ContributionStatus status = ContributionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PAYMENT_NOT_REQUIRED;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    private Long submittedByUserId;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime reviewedAt;

    private String reviewedBy;

    public enum ContributionStatus {
        PENDING, APPROVED, REJECTED
    }

    public enum PaymentStatus {
        PAYMENT_NOT_REQUIRED, PAYMENT_PENDING, PAYMENT_PAID, PAYMENT_FAILED, PAYMENT_REFUNDED
    }
}
