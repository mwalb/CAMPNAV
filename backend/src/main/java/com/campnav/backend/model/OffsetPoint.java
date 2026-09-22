package com.campnav.backend.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class OffsetPoint {
    private float x;
    private float y;
}
