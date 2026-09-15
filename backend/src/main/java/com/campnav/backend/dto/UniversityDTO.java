package com.campnav.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityDTO {
    private Long id;
    private String externalId;
    private String name;
    private String officialName;
    private String shortName;
    private String logoUrl;
    private String description;
    private String country;
    private String city;
    private String status;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private Double latitude;
    private Double longitude;
    private Float defaultZoom;
    private Integer sortOrder;
    private Double campusAreaHectares;
    private Boolean isActive;
}
