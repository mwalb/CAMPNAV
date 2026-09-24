-- Seed MUST (Mbeya University of Science and Technology) dataset with 7 categories and 17 verified campus locations

-- 1. ENSURE MUST UNIVERSITY RECORD EXISTS (University ID 3)
INSERT INTO university (id, external_id, name, official_name, short_name, city, country, latitude, longitude, default_zoom, description, logo_url, sort_order, is_active, status)
SELECT 3, 'MUST', 'Mbeya University of Science and Technology', 'Mbeya University of Science and Technology', 'MUST', 'Mbeya', 'Tanzania', -8.94315, 33.41636, 15.0, 'Science and Technology for Development', 'https://upload.wikimedia.org/wikipedia/en/a/a2/Mbeya_University_of_Science_and_Technology_Logo.png', 3, true, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM university WHERE id = 3 OR external_id = 'MUST');

-- Ensure campus record exists for MUST
INSERT INTO campus (id, external_id, university_id, name)
SELECT 3, 'MUST-MAIN', 3, 'Mbeya University Main Campus'
WHERE NOT EXISTS (SELECT 1 FROM campus WHERE id = 3 OR external_id = 'MUST-MAIN');

-- 2. CLEANUP EXISTING MUST DATA
DELETE FROM campus_location WHERE university_id = 3;
DELETE FROM category WHERE university_id = 3;

-- 3. INSERT CATEGORIES FOR MUST (University ID 3)
INSERT INTO category (id, university_id, name, slug, icon_name, description, is_active, sort_order) VALUES
(25, 3, 'Academic Facilities', 'must-academic', 'school', 'Departments, Classrooms, Workshops and Lecture Blocks', true, 1),
(26, 3, 'Administrative Offices', 'must-administration', 'business', 'Main Administration and Faculty Offices', true, 2),
(27, 3, 'Libraries & Research', 'must-library', 'menu_book', 'Libraries and Research Facilities', true, 3),
(28, 3, 'Student Accommodation & Hostels', 'must-hostel', 'hotel', 'Student Hostels and Residential Blocks', true, 4),
(29, 3, 'Services & Shops', 'must-services', 'shopping_cart', 'Stationery, Commercial and Printing Services', true, 5),
(30, 3, 'Student Facilities & Recreation', 'must-recreation', 'park', 'Student Clubs, Vimbweta and Gardens', true, 6),
(31, 3, 'Sports & Playgrounds', 'must-sports', 'sports_soccer', 'Basketball Grounds, Sports Fields and Grounds', true, 7);

-- 4. INSERT MUST LOCATIONS WITH VERIFIED COORDINATES FROM PDF

-- CATEGORY 25: ACADEMIC FACILITIES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(3, 25, 'Arch & Civil Engineering Department', 'Arch & Civil Engineering Department', 'Civil Engineering', 'MUST-CIVIL', 'Arch & Civil Engineering Department', -8.94229, 33.41575, 'VERIFIED', true),
(3, 25, 'Classes Block', 'Classes Block', 'Lecture Classes', 'MUST-CLASS', 'Main Classes Block', -8.94249, 33.41663, 'VERIFIED', true),
(3, 25, 'NLH 4 & 5', 'New Lecture Halls 4 & 5', 'NLH 4 & 5', 'MUST-NLH', 'Lecture Halls NLH 4 & 5', -8.94308, 33.41765, 'VERIFIED', true),
(3, 25, 'CITT Building', 'CITT Building', 'CITT', 'MUST-CITT', 'Center for Innovation and Technology Transfer', -8.94478, 33.41892, 'VERIFIED', true),
(3, 25, 'New Academic Blocks', 'New Academic Blocks', 'Academic Blocks', 'MUST-ACAD', 'New Academic Complex Blocks', -8.94488, 33.41596, 'VERIFIED', true),
(3, 25, 'New Workshops', 'New Workshops Building', 'Workshops', 'MUST-WORK', 'Engineering and Technical Workshops', -8.94507, 33.41750, 'VERIFIED', true);

-- CATEGORY 26: ADMINISTRATIVE OFFICES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(3, 26, 'Mbeya University of Science and Technology (MUST)', 'Mbeya University of Science and Technology Main Entry', 'MUST Main', 'MUST-HQ', 'Main Campus Administration & Central POI', -8.94315, 33.41636, 'VERIFIED', true),
(3, 26, 'Administration Blocks', 'MUST Administration Blocks', 'Central Admin', 'MUST-ADM', 'Main Administration Blocks', -8.94211, 33.41612, 'VERIFIED', true),
(3, 26, 'Administration Block CoICT', 'Administration Block CoICT', 'CoICT Admin', 'MUST-CoICT', 'Administration Block College of Information and Communication Technology', -8.94272, 33.41343, 'VERIFIED', true);

-- CATEGORY 27: LIBRARIES & RESEARCH
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(3, 27, 'Dr. Magufuli Library', 'Dr. Magufuli Library MUST', 'Magufuli Library', 'MUST-LIB', 'Main Campus Dr. Magufuli Library', -8.94334, 33.41911, 'VERIFIED', true);

-- CATEGORY 28: STUDENT ACCOMMODATION & HOSTELS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(3, 28, 'New Hostels', 'MUST New Student Hostels', 'Hostels', 'MUST-HOSTEL', 'Student Residential Hostels', -8.94418, 33.41214, 'VERIFIED', true);

-- CATEGORY 29: SERVICES & SHOPS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(3, 29, 'MUST Stationery', 'MUST Stationery & Printing', 'Stationery', 'MUST-STAT', 'Stationery, Printing and Photocopy Services', -8.93965, 33.41761, 'VERIFIED', true);

-- CATEGORY 30: STUDENT FACILITIES & RECREATION
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(3, 30, 'MUST Club', 'MUST Social Club', 'MUST Club', 'MUST-CLUB', 'Student and Staff Social Club', -8.94012, 33.41766, 'VERIFIED', true),
(3, 30, 'Vimbweta vya 6B', 'Vimbweta vya 6B Study Area', 'Vimbweta 6B', 'MUST-VIMB', 'Outdoor Student Study & Relaxation Area', -8.94074, 33.41663, 'VERIFIED', true),
(3, 30, 'MUST Garden', 'MUST Botanical Garden', 'Garden', 'MUST-GARDEN', 'MUST Recreation Garden', -8.94046, 33.41612, 'VERIFIED', true);

-- CATEGORY 31: SPORTS & PLAYGROUNDS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(3, 31, 'BASKET GROUND', 'MUST Basketball Ground', 'Basket Ground', 'MUST-BB', 'MUST Basketball Court Ground', -8.94304, 33.41596, 'VERIFIED', true),
(3, 31, 'Shamba la Babu', 'Shamba la Babu Sports Ground', 'Shamba la Babu', 'MUST-PITCH', 'MUST Sports & Football Ground', -8.93842, 33.41877, 'VERIFIED', true);

-- SYNC SEQUENCES FOR H2 / POSTGRESQL
ALTER TABLE university ALTER COLUMN id RESTART WITH 10;
ALTER TABLE campus ALTER COLUMN id RESTART WITH 10;
ALTER TABLE category ALTER COLUMN id RESTART WITH 50;
