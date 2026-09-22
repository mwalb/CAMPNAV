package com.campnav.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ShapeOverlay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId;

    private String id;

    @Enumerated(EnumType.STRING)
    private ShapeType type;

    private float x;
    private float y;
    private float width;
    private float height;
    private long color;
    private float alpha;
    private boolean isFilled;
}
