package com.campnav.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class DrawingPath {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId;

    private String id;

    @ElementCollection
    @CollectionTable(name = "drawing_points", joinColumns = @JoinColumn(name = "drawing_id"))
    private List<OffsetPoint> points;

    private long color;
    private float thickness;
    private float opacity;

    @Enumerated(EnumType.STRING)
    private StrokeStyle style;

    @Enumerated(EnumType.STRING)
    private BrushType brushType;

    @ElementCollection
    private List<Long> gradientColors;
}
