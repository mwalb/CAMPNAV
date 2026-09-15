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
    private String externalId;
    private String name;
    private String officialName;
    private String aliases;
    private String description;
    private Long universityId;
    private Long campusId;
    private Long categoryId;
    private Double latitude;
    private Double longitude;
    private Double entranceLatitude;
    private Double entranceLongitude;
    private String coordinateType;
    private String confidence;
    private String status;
    private String buildingCode;
    private String floor;
    private String roomNumber;
    
    private String googlePlaceId;
    private String googleName;
    private String googleAddress;
    private String googleType;
    private String verificationStatus;
    private String source;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    
    private String imageUrl;
    private String phone;
    private String email;
    private String openingHours;
    private Boolean isActive;
    private String sourcePrimary;
    private String sourceSecondary;
    private String sourceMap;
    private String sourceNotes;
    private String verifiedDate;
}
