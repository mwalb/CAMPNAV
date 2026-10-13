package com.campnav.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "university_office_referral")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityOfficeReferral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private IssueReport report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referring_leader_id", nullable = false)
    private CommunityUser referringLeader;

    @Column(nullable = false)
    private String officeName;

    private String officeContact;

    private String referralChannel; // EMAIL, PHONE, IN_PERSON, OFFICIAL_LETTER

    @Builder.Default
    private LocalDateTime referralDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OfficeAckStatus officeAckStatus = OfficeAckStatus.PENDING_ACK;

    @Column(columnDefinition = "TEXT")
    private String officeResponse;

    private LocalDateTime followUpDate;

    @Column(columnDefinition = "TEXT")
    private String outcome;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum OfficeAckStatus {
        PENDING_ACK, ACKNOWLEDGED, REJECTED, ACTION_TAKEN
    }
}
