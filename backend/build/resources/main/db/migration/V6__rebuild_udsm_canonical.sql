-- Rebuild UDSM Data with Canonical Info and Google Place IDs

-- Ensure University table has recommended structure
ALTER TABLE university ADD COLUMN IF NOT EXISTS official_name VARCHAR(255);
ALTER TABLE university ADD COLUMN IF NOT EXISTS status VARCHAR(50);
ALTER TABLE university ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE university ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Update UDSM University entry
UPDATE university SET
    name = 'University of Dar es Salaam',
    official_name = 'University of Dar es Salaam',
    city = 'Dar es Salaam',
    country = 'Tanzania',
    latitude = -6.7801,
    longitude = 39.2041,
    is_active = true,
    status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE short_name = 'UDSM';

-- Category System Reset
DELETE FROM campus_location WHERE university_id IN (SELECT id FROM university WHERE short_name = 'UDSM');
DELETE FROM category;

INSERT INTO category (name, slug, description, icon_name, is_active, sort_order) VALUES
('Academic & Learning', 'academic', 'Colleges, Schools and Departments', 'school', true, 1),
('Accommodation', 'hostel', 'Halls of residence and hostels', 'hotel', true, 2),
('Food & Cafeteria', 'food', 'Cafeterias and food points', 'restaurant', true, 3),
('Library & Study', 'library', 'Libraries and study spaces', 'menu_book', true, 4),
('Health & Medical', 'health', 'Hospital and dispensaries', 'medical_services', true, 5),
('Banks & Finance', 'banking', 'Banks and financial services', 'payments', true, 6),
('Administration', 'administration', 'Administrative offices', 'business', true, 7),
('Student Services', 'student_services', 'DARUSO, USAB and student governance', 'group', true, 8),
('Sports & Recreation', 'sports', 'Sports grounds and gym', 'sports_soccer', true, 9),
('Transport & Parking', 'transport', 'Shuttle and bus stops', 'directions_bus', true, 10),
('Security & Emergency', 'security', 'Police and security posts', 'security', true, 11),
('Technology & ICT', 'ict', 'Computer centers and virtual learning', 'computer', true, 12),
('Religious Places', 'religious', 'Chapels and Mosques', 'account_balance', true, 13),
('Shopping & Services', 'shopping', 'Shops and bookstores', 'shopping_cart', true, 14),
('Landmarks & Places', 'landmarks', 'Notable campus landmarks', 'place', true, 15);

-- Ensure CampusLocation table matches recommended structure
ALTER TABLE campus_location ADD COLUMN IF NOT EXISTS google_place_id VARCHAR(255);
ALTER TABLE campus_location ADD COLUMN IF NOT EXISTS google_name VARCHAR(255);
ALTER TABLE campus_location ADD COLUMN IF NOT EXISTS google_address TEXT;
ALTER TABLE campus_location ADD COLUMN IF NOT EXISTS google_type VARCHAR(100);
ALTER TABLE campus_location ADD COLUMN IF NOT EXISTS verification_status VARCHAR(50) DEFAULT 'NEEDS_REVIEW';
ALTER TABLE campus_location ADD COLUMN IF NOT EXISTS source VARCHAR(255);
ALTER TABLE campus_location ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE campus_location ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Create Indexes
CREATE INDEX IF NOT EXISTS idx_location_university ON campus_location(university_id);
CREATE INDEX IF NOT EXISTS idx_location_category ON campus_location(category_id);
CREATE INDEX IF NOT EXISTS idx_location_verification ON campus_location(verification_status);
CREATE INDEX IF NOT EXISTS idx_location_google_id ON campus_location(google_place_id);
CREATE INDEX IF NOT EXISTS idx_location_coords ON campus_location(latitude, longitude);
