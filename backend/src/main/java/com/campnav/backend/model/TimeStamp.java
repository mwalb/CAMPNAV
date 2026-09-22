package com.campnav.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId;

    private float time;
    @Column(name = "overlay_text")
    private String text;
    private long color;
}
