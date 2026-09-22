package com.campnav.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CounterOverlay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId;

    private String id;
    @Column(name = "overlay_count")
    private int count;
    private String label;
    private float x;
    private float y;
    private float fontSize;
    private long color;
}
