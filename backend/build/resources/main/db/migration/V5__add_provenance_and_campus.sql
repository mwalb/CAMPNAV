-- Update University
ALTER TABLE university ADD COLUMN external_id VARCHAR(50);
UPDATE university SET external_id = short_name;

-- Create Campus Table
CREATE TABLE campus (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(50),
    name VARCHAR(255) NOT NULL,
    university_id BIGINT REFERENCES university(id)
);

-- Update Campus Location
ALTER TABLE campus_location ADD COLUMN external_id VARCHAR(50);
ALTER TABLE campus_location ADD COLUMN official_name VARCHAR(255);
ALTER TABLE campus_location ADD COLUMN aliases TEXT;
ALTER TABLE campus_location ADD COLUMN campus_id BIGINT REFERENCES campus(id);
ALTER TABLE campus_location ADD COLUMN entrance_latitude DOUBLE PRECISION;
ALTER TABLE campus_location ADD COLUMN entrance_longitude DOUBLE PRECISION;
ALTER TABLE campus_location ADD COLUMN coordinate_type VARCHAR(50);
ALTER TABLE campus_location ADD COLUMN confidence VARCHAR(50);
ALTER TABLE campus_location ADD COLUMN status VARCHAR(50);
ALTER TABLE campus_location ADD COLUMN source_primary VARCHAR(255);
ALTER TABLE campus_location ADD COLUMN source_secondary VARCHAR(255);
ALTER TABLE campus_location ADD COLUMN source_map VARCHAR(255);
ALTER TABLE campus_location ADD COLUMN source_notes TEXT;
ALTER TABLE campus_location ADD COLUMN verified_date VARCHAR(50);

-- Categorize as TEXT where needed for longer content
ALTER TABLE campus_location ALTER COLUMN description TYPE TEXT;
