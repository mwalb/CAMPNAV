package com.campnav.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class University {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String externalId; // e.g., UDSM
    private String name;
    private String officialName;
    private String shortName;
    private String logoUrl;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String country;
    private String city;
    private String status;
    
    @org.hibernate.annotations.CreationTimestamp
    private java.time.LocalDateTime createdAt;
    @org.hibernate.annotations.UpdateTimestamp
    private java.time.LocalDateTime updatedAt;
    
    private Double latitude;
    private Double longitude;
    private Float defaultZoom;
    
    @Builder.Default
    private Boolean isActive = true;
    
    private Integer sortOrder;
    
    private Double campusAreaHectares;
    
    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<CampusLocation> locations;
}
