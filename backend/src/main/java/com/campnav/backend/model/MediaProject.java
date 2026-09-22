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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "project_id")
    private List<BubbleOverlay> bubbles;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "project_id")
    private List<DrawingPath> drawings;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "project_id")
    private List<ShapeOverlay> shapes;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "project_id")
    private List<StickerOverlay> stickers;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "project_id")
    private List<MusicOverlay> musicOverlays;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "project_id")
    private List<ProgressOverlay> progressOverlays;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "project_id")
    private List<CounterOverlay> counterOverlays;

    @ElementCollection
    @CollectionTable(name = "project_text_animations", joinColumns = @JoinColumn(name = "project_id"))
    @MapKeyColumn(name = "text_key")
    @Column(name = "animation_type")
    @Enumerated(EnumType.STRING)
    private Map<String, TextAnimationType> textAnimations;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "project_id")
    private List<TimeStamp> timeStamps;

    private String previewUri;
    private Long lastModified;
}
