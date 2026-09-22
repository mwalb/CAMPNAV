package com.campnav.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class StickerOverlay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId;

    private String id;
    private String emoji;
    private float x;
    private float y;
    private float size;
    private float rotation;
    private float opacity;
}
