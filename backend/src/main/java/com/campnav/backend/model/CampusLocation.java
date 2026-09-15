package com.campnav.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampusLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String externalId; // e.g., LOC-0001
    
    private String name;
    private String officialName;
    private String aliases;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "university_id")
    @JsonIgnore
    private University university;
    
    @ManyToOne
    @JoinColumn(name = "campus_id")
    private Campus campus;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
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
    @Column(columnDefinition = "TEXT")
    private String googleAddress;
    private String googleType;
    
    private String verificationStatus; // e.g., VERIFIED, NEEDS_REVIEW
    private String source;
    
    @org.hibernate.annotations.CreationTimestamp
    private java.time.LocalDateTime createdAt;
    @org.hibernate.annotations.UpdateTimestamp
    private java.time.LocalDateTime updatedAt;
    
    private String imageUrl;
    private String phone;
    private String email;
    private String openingHours;
    
    private String sourcePrimary;
    private String sourceSecondary;
    private String sourceMap;
    @Column(columnDefinition = "TEXT")
    private String sourceNotes;
    private String verifiedDate;
    
    @Builder.Default
    private Boolean isActive = true;
}
