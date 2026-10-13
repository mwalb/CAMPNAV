package com.campnav.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private IssueReport report;

    @Column(nullable = false)
    private String recipientEmail;

    @Column(nullable = false)
    private String recipientRole; // LEADER, REPORTER, ADMIN

    @Column(nullable = false)
    private String notificationType; // NEW_REPORT_LEADER, REPORT_STATUS_UPDATE_REPORTER, MISSING_LEADER_ADMIN, DELIVERY_FAILURE_ADMIN, OVERDUE_ACK_ADMIN

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String bodyText;

    @Column(columnDefinition = "TEXT")
    private String bodyHtml;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private Integer attemptCount = 0;

    private LocalDateTime lastAttemptAt;
    private LocalDateTime nextRetryAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeliveryState deliveryState = DeliveryState.PENDING;

    private String providerMessageId;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    private LocalDateTime submittedAt;

    public enum DeliveryState {
        PENDING, PROCESSING, SENT_TO_PROVIDER, DELIVERED, RETRY_PENDING, FAILED
    }
}
