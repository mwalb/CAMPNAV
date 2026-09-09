package com.campnav.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampusLocationDTO {
    private Long id;
    private String name;
    private String description;
    private Long universityId;
    private Long categoryId;
    private Double latitude;
    private Double longitude;
    private String buildingCode;
    private String floor;
    private String roomNumber;
    private String imageUrl;
    private String phone;
    private String email;
    private String openingHours;
}
