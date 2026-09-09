package com.campnav.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaItem {
    @Id
    private String id;
    private String name;
    
    @Enumerated(EnumType.STRING)
    private MediaType type;
    
    private String uri;
    private String thumbnail;
    private Long createdAt;
}
