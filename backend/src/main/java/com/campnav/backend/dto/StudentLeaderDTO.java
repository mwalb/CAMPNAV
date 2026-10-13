package com.campnav.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentLeaderDTO {
    private Long id;
    private Long leaderUserId;
    private String leaderUsername;
    private String leaderFullName;
    private String leaderEmail;
    private String leaderPhone;
    private Long universityId;
    private String universityName;
    private String status;
    private String appointedByAdminUsername;
    private LocalDateTime appointmentDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String deactivationReason;
}
