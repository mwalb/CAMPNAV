package com.campnav.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_activity_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String actorUsername;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private String relatedEntityId;

    private String ipAddress;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
