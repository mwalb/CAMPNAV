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
