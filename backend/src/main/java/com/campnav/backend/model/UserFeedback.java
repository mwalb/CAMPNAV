package com.campnav.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String feedbackText;

    private String contributorName;

    private String contributorContact;

    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    private String rejectionReason;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
