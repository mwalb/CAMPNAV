package com.campnav.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class MusicOverlay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId;

    private String id;
    private String title;
    private String artist;
    private float x;
    private float y;
    private float size;
    private boolean isPlaying;
}
