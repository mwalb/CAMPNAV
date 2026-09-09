package com.campnav.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Entity
@Data
public class MediaProject {
    @Id
    private String id;
    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    private MediaItem originalMedia;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "project_id")
    private List<EditAction> editHistory;

    // Simplified for now, could be OneToMany with specific entities
    @ElementCollection
    private List<String> textAnimations; 

    private String previewUri;
    private Long lastModified;
}
