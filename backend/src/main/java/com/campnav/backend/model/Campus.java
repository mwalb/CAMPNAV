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
public class Campus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String externalId; // e.g., UDSM-MLIMANI
    private String name;
    
    @ManyToOne
    @JoinColumn(name = "university_id")
    @JsonIgnore
    private University university;
    
    @OneToMany(mappedBy = "campus", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<CampusLocation> locations;
}
