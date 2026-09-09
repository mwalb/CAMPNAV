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
    
    private String name;
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "university_id")
    @JsonIgnore
    private University university;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
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
