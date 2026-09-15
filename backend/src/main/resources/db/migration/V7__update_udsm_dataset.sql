-- Update Category schema to support university-specific categories
ALTER TABLE category ADD COLUMN IF NOT EXISTS university_id BIGINT REFERENCES university(id);

-- Ensure UDSM University exists with ID 1
INSERT INTO university (id, external_id, name, short_name, city, country, latitude, longitude, is_active)
SELECT 1, 'UDSM', 'University of Dar es Salaam', 'UDSM', 'Dar es Salaam', 'Tanzania', -6.7801, 39.2041, true
WHERE NOT EXISTS (SELECT 1 FROM university WHERE id = 1);

-- Sync university sequence for H2
ALTER TABLE university ALTER COLUMN id RESTART WITH 2;

-- Clear UDSM data to rebuild
DELETE FROM campus_location WHERE university_id = 1;
DELETE FROM category WHERE university_id = 1 OR university_id IS NULL;

-- Insert Category Records for UDSM Mlimani Main Campus (University ID 1)
INSERT INTO category (id, university_id, name, icon_name, description, slug, is_active, sort_order) VALUES
(1, 1, 'Academic Colleges & Schools', 'school', 'Faculties, Colleges, and Lecture Halls', 'academic', true, 1),
(2, 1, 'Halls & Hostels', 'hotel', 'Student Accommodations', 'hostel', true, 2),
(3, 1, 'Administrative & Services', 'corporate_fare', 'Administrative Offices, Banks, and Student Services', 'administration', true, 3),
(4, 1, 'Libraries & Computing', 'menu_book', 'Library and Tech Infrastructure', 'library', true, 4),
(5, 1, 'Health & Recreation', 'local_hospital', 'Medical Centers, Sports Fields, and Cafeterias', 'health', true, 5);

-- Reset sequence for category
ALTER TABLE category ALTER COLUMN id RESTART WITH 6;

-- Campus Location Records with Exact Google Maps POI Names
INSERT INTO campus_location
(university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active)
VALUES
-- Colleges & Faculties
(1, 1, 'CoET - College of Engineering and Technology', 'College of Engineering and Technology', 'CoET; Engineering Block; Block A B C', 'CoET', 'Main Engineering Campus Complex including Block A, B, and C', -6.7792, 39.2045, 'VERIFIED', true),
(1, 1, 'CoNAS - College of Natural and Applied Sciences', 'College of Natural and Applied Sciences', 'CoNAS; Science Complex', 'CoNAS', 'Departments of Physics, Chemistry, Zoology, and Mathematics', -6.7818, 39.2081, 'VERIFIED', true),
(1, 1, 'CoHU - College of Humanities', 'College of Humanities', 'CoHU; Humanities Building', 'CoHU', 'College housing Arts, Languages, and History departments', -6.7788, 39.2089, 'VERIFIED', true),
(1, 1, 'CoSS - College of Social Sciences', 'College of Social Sciences', 'CoSS; Social Sciences', 'CoSS', 'Departments of Economics, Sociology, and Political Science', -6.7801, 39.2072, 'VERIFIED', true),
(1, 1, 'UDBS - University of Dar es Salaam Business School', 'University of Dar es Salaam Business School', 'UDBS; Business School', 'UDBS', 'Business, Finance, and Management studies department', -6.7772, 39.2039, 'VERIFIED', true),
(1, 1, 'UDSoL - University of Dar es Salaam School of Law', 'University of Dar es Salaam School of Law', 'UDSoL; Law Faculty', 'UDSoL', 'Main Law Faculty Building', -6.7765, 39.2052, 'VERIFIED', true),
(1, 1, 'SoED - School of Education', 'School of Education', 'SoED; Education Complex', 'SoED', 'Faculty of Education and Teacher Training', -6.7825, 39.2061, 'VERIFIED', true),
(1, 1, 'Yombo Lecture Theatre Complex', 'Yombo Lecture Theatres', 'Yombo 1; Yombo 2; Yombo 3; YOMBO', 'YLT', 'Major campus lecture halls cluster (Yombo 1 to Yombo 5)', -6.7808, 39.2058, 'VERIFIED', true),
(1, 1, 'Nkrumah Hall', 'Nkrumah Hall UDSM', 'Nkrumah; Main Assembly Hall', 'NK', 'Historic central auditorium used for main events and lectures', -6.7798, 39.2075, 'VERIFIED', true),

-- Libraries & Tech Centres
(1, 4, 'UDSM New Library', 'Dr. Wilbert Chagula Library (New Wing)', 'Chinese Library; New Library UDSM', 'NLIB', 'The mega modern library near Yombo Complex', -6.7812, 39.2048, 'VERIFIED', true),
(1, 4, 'Old Campus Library', 'Old Chagula Library', 'Old Library', 'OLIB', 'Original campus main library building', -6.7795, 39.2070, 'VERIFIED', true),
(1, 4, 'UCC - University Computing Centre', 'University Computing Centre UDSM', 'UCC; Computer Center', 'UCC', 'IT and computing services center', -6.7785, 39.2031, 'VERIFIED', true),

-- Halls of Residence
(1, 2, 'Hall 1 - Mkwawa', 'Hall 1 Mkwawa Hostel', 'Hall 1; Mkwawa', 'H1', 'Student Hall of Residence 1', -6.7830, 39.2078, 'VERIFIED', true),
(1, 2, 'Hall 2 - Kilwa', 'Hall 2 Kilwa Hostel', 'Hall 2; Kilwa', 'H2', 'Student Hall of Residence 2', -6.7835, 39.2082, 'VERIFIED', true),
(1, 2, 'Hall 3 - Kibo', 'Hall 3 Kibo Hostel', 'Hall 3; Kibo', 'H3', 'Student Hall of Residence 3', -6.7840, 39.2085, 'VERIFIED', true),
(1, 2, 'Hall 4 - Mawenzi', 'Hall 4 Mawenzi Hostel', 'Hall 4; Mawenzi', 'H4', 'Student Hall of Residence 4', -6.7845, 39.2088, 'VERIFIED', true),
(1, 2, 'Hall 5 - Luthuli', 'Hall 5 Luthuli Hostel', 'Hall 5; Luthuli', 'H5', 'Student Hall of Residence 5', -6.7850, 39.2091, 'VERIFIED', true),
(1, 2, 'Hall 6 - Kimweri', 'Hall 6 Kimweri Hostel', 'Hall 6; Kimweri', 'H6', 'Student Hall of Residence 6', -6.7855, 39.2095, 'VERIFIED', true),
(1, 2, 'Hall 7 - Magufuli', 'Hall 7 Hostel', 'Hall 7', 'H7', 'Student Hall of Residence 7', -6.7860, 39.2100, 'VERIFIED', true),
(1, 2, 'Magufuli Hostels UDSM', 'Hon. Dr. John Pombe Joseph Magufuli Hostels', 'Hostel Mpya; Magufuli Hostels', 'MHOSTEL', 'Large modern hostel complex located near Ubungo boundary', -6.7885, 39.2012, 'VERIFIED', true),

-- Administration & Student Services
(1, 3, 'UDSM Administration Block', 'UDSM Main Administration Building', 'Admin Block; Vice Chancellor Office', 'ADMIN', 'Central university offices including Admissions and VC office', -6.7790, 39.2068, 'VERIFIED', true),
(1, 3, 'DARUSO Government Office', 'DARUSO Head Office', 'DARUSO; Student Union', 'DARUSO', 'Student government headquarters', -6.7802, 39.2069, 'VERIFIED', true),
(1, 3, 'CRDB Bank UDSM Branch', 'CRDB Bank Mlimani Campus', 'CRDB UDSM', 'CRDB', 'Full service branch located within campus center', -6.7781, 39.2062, 'VERIFIED', true),
(1, 3, 'NMB Bank UDSM Branch', 'NMB Bank Mlimani Campus', 'NMB UDSM', 'NMB', 'NMB Campus Branch and ATM area', -6.7783, 39.2060, 'VERIFIED', true),

-- Health & Recreation
(1, 5, 'UDSM Health Centre (Hospital)', 'UDSM Health Centre', 'UDSM Hospital; Campus Dispensary', 'HEALTH', 'Main medical facility for students and staff on campus', -6.7820, 39.2025, 'VERIFIED', true),
(1, 5, 'Mlimani Campus Sports Grounds', 'UDSM Sports Complex', 'UDSM Pitch; Sports Ground', 'SPORTS', 'Football pitches, basketball courts, and athletics tracks', -6.7842, 39.2050, 'VERIFIED', true),
(1, 5, 'Yombo Cafeteria', 'Yombo Student Canteen', 'Yombo Cafeteria', 'YCAF', 'Major dining location near Yombo lecture rooms', -6.7810, 39.2055, 'VERIFIED', true);
