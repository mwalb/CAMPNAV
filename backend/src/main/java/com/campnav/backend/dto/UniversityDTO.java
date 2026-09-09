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
    private String name;
    private String shortName;
    private String logoUrl;
    private String description;
    private String country;
    private String city;
    private Double latitude;
    private Double longitude;
    private Float defaultZoom;
}
