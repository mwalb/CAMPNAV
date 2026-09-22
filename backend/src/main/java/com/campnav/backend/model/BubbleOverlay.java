package com.campnav.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class BubbleOverlay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId;
    
    private String id;
    @Column(name = "overlay_text")
    private String text;
    private float x;
    private float y;
    
    @Enumerated(EnumType.STRING)
    private BubbleType type;
    
    private long color;
    private float fontSize;
    private float rotation;
    private float scale;
    private float opacity;
    private long textColor;
    private float shadowRadius;
    private float borderWidth;
    private long borderColor;
    private float cornerRadius;
    private float paddingHorizontal;
    private float paddingVertical;
    private float glowIntensity;
    private float animationSpeed;
    private float waveAmplitude;
    private String emoji;
    private boolean fontStyleItalic;
    private boolean fontWeightBold;
    private boolean hasShadow;
    private boolean hasBorder;
    private boolean isAnimated;
    private float pulseSpeed;

    @ElementCollection
    private List<Long> gradientColors;

    @Enumerated(EnumType.STRING)
    private StrokeStyle strokeStyle;
    
    private int starCount;
    private float sparkleIntensity;
    private float blurAmount;
    private boolean isFlipped;
    private String fontFamily;
}
