-- MEDIA EDITOR OVERLAYS SCHEMA

CREATE TABLE bubble_overlay (
    internal_id BIGSERIAL PRIMARY KEY,
    id VARCHAR(255),
    project_id VARCHAR(255) REFERENCES media_project(id),
    overlay_text TEXT,
    x FLOAT,
    y FLOAT,
    type VARCHAR(50),
    color BIGINT,
    font_size FLOAT,
    rotation FLOAT,
    scale FLOAT,
    opacity FLOAT,
    text_color BIGINT,
    shadow_radius FLOAT,
    border_width FLOAT,
    border_color BIGINT,
    corner_radius FLOAT,
    padding_horizontal FLOAT,
    padding_vertical FLOAT,
    glow_intensity FLOAT,
    animation_speed FLOAT,
    wave_amplitude FLOAT,
    emoji VARCHAR(50),
    font_style_italic BOOLEAN,
    font_weight_bold BOOLEAN,
    has_shadow BOOLEAN,
    has_border BOOLEAN,
    is_animated BOOLEAN,
    pulse_speed FLOAT,
    stroke_style VARCHAR(50),
    star_count INTEGER,
    sparkle_intensity FLOAT,
    blur_amount FLOAT,
    is_flipped BOOLEAN,
    font_family VARCHAR(255)
);

CREATE TABLE bubble_overlay_gradient_colors (
    bubble_overlay_internal_id BIGINT REFERENCES bubble_overlay(internal_id),
    gradient_colors BIGINT
);

CREATE TABLE drawing_path (
    internal_id BIGSERIAL PRIMARY KEY,
    id VARCHAR(255),
    project_id VARCHAR(255) REFERENCES media_project(id),
    color BIGINT,
    thickness FLOAT,
    opacity FLOAT,
    style VARCHAR(50),
    brush_type VARCHAR(50)
);

CREATE TABLE drawing_points (
    drawing_id BIGINT REFERENCES drawing_path(internal_id),
    x FLOAT,
    y FLOAT
);

CREATE TABLE drawing_path_gradient_colors (
    drawing_path_internal_id BIGINT REFERENCES drawing_path(internal_id),
    gradient_colors BIGINT
);

CREATE TABLE shape_overlay (
    internal_id BIGSERIAL PRIMARY KEY,
    id VARCHAR(255),
    project_id VARCHAR(255) REFERENCES media_project(id),
    type VARCHAR(50),
    x FLOAT,
    y FLOAT,
    width FLOAT,
    height FLOAT,
    color BIGINT,
    alpha FLOAT,
    is_filled BOOLEAN
);

CREATE TABLE sticker_overlay (
    internal_id BIGSERIAL PRIMARY KEY,
    id VARCHAR(255),
    project_id VARCHAR(255) REFERENCES media_project(id),
    emoji VARCHAR(50),
    x FLOAT,
    y FLOAT,
    size FLOAT,
    rotation FLOAT,
    opacity FLOAT
);

CREATE TABLE music_overlay (
    internal_id BIGSERIAL PRIMARY KEY,
    id VARCHAR(255),
    project_id VARCHAR(255) REFERENCES media_project(id),
    title VARCHAR(255),
    artist VARCHAR(255),
    x FLOAT,
    y FLOAT,
    size FLOAT,
    is_playing BOOLEAN
);

CREATE TABLE progress_overlay (
    internal_id BIGSERIAL PRIMARY KEY,
    id VARCHAR(255),
    project_id VARCHAR(255) REFERENCES media_project(id),
    overlay_value FLOAT,
    x FLOAT,
    y FLOAT,
    width FLOAT,
    height FLOAT,
    color BIGINT,
    background_color BIGINT
);

CREATE TABLE counter_overlay (
    internal_id BIGSERIAL PRIMARY KEY,
    id VARCHAR(255),
    project_id VARCHAR(255) REFERENCES media_project(id),
    overlay_count INTEGER,
    label VARCHAR(255),
    x FLOAT,
    y FLOAT,
    font_size FLOAT,
    color BIGINT
);

CREATE TABLE time_stamp (
    internal_id BIGSERIAL PRIMARY KEY,
    project_id VARCHAR(255) REFERENCES media_project(id),
    time FLOAT,
    overlay_text TEXT,
    color BIGINT
);

CREATE TABLE project_text_animations (
    project_id VARCHAR(255) REFERENCES media_project(id),
    text_key VARCHAR(255),
    animation_type VARCHAR(50),
    PRIMARY KEY (project_id, text_key)
);
