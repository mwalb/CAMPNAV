package com.campnav.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ProgressOverlay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId;

    private String id;
    @Column(name = "overlay_value")
    private float value;
    private float x;
    private float y;
    private float width;
    private float height;
    private long color;
    private long backgroundColor;
}
