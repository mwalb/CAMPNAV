CREATE TABLE university (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    short_name VARCHAR(50),
    logo_url VARCHAR(255),
    description TEXT,
    country VARCHAR(100),
    city VARCHAR(100),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    default_zoom REAL
);

CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    icon_name VARCHAR(50)
);

CREATE TABLE campus_location (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    university_id BIGINT REFERENCES university(id),
    category_id BIGINT REFERENCES category(id),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    building_code VARCHAR(50),
    floor VARCHAR(20),
    room_number VARCHAR(20),
    image_url VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(100),
    opening_hours VARCHAR(255)
);

CREATE TABLE channel (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    url VARCHAR(255),
    logo VARCHAR(255),
    category VARCHAR(100)
);

CREATE TABLE playlist (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    url VARCHAR(255)
);

CREATE TABLE playlist_channels (
    playlist_id BIGINT REFERENCES playlist(id),
    channels_id BIGINT REFERENCES channel(id)
);

CREATE TABLE media_item (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    type VARCHAR(50),
    uri VARCHAR(255),
    thumbnail VARCHAR(255),
    created_at BIGINT
);

CREATE TABLE media_project (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    original_media_id VARCHAR(255) REFERENCES media_item(id),
    preview_uri VARCHAR(255),
    last_modified BIGINT
);

CREATE TABLE edit_action (
    id BIGSERIAL PRIMARY KEY,
    operation VARCHAR(50),
    project_id VARCHAR(255) REFERENCES media_project(id)
);

CREATE TABLE edit_action_params (
    edit_action_id BIGINT REFERENCES edit_action(id),
    param_value VARCHAR(255),
    param_key VARCHAR(255),
    PRIMARY KEY (edit_action_id, param_key)
);

CREATE TABLE media_project_text_animations (
    media_project_id VARCHAR(255) REFERENCES media_project(id),
    text_animations VARCHAR(255)
);
