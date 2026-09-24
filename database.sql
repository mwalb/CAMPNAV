-- CAMPNAV CANONICAL DATABASE DUMP
-- Combined Schema and Data for GitHub
-- Generated on 2026-09-20

-- Drop existing tables to ensure a clean state
DROP TABLE IF EXISTS campus_location CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS campus CASCADE;
DROP TABLE IF EXISTS university CASCADE;

-- 1. UNIVERSITY SCHEMA
CREATE TABLE university (
    id SERIAL PRIMARY KEY,
    external_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    official_name VARCHAR(255),
    short_name VARCHAR(50),
    city VARCHAR(100),
    country VARCHAR(100),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    default_zoom FLOAT DEFAULT 15.0,
    description TEXT,
    logo_url TEXT,
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    campus_area_hectares DOUBLE PRECISION,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. CATEGORY SCHEMA
CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    university_id INTEGER REFERENCES university(id),
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100),
    icon_name VARCHAR(50),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INTEGER DEFAULT 0,
    UNIQUE(university_id, slug)
);

-- 3. CAMPUS SCHEMA
CREATE TABLE campus (
    id SERIAL PRIMARY KEY,
    external_id VARCHAR(50) UNIQUE NOT NULL,
    university_id INTEGER REFERENCES university(id),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. CAMPUS_LOCATION SCHEMA
CREATE TABLE campus_location (
    id SERIAL PRIMARY KEY,
    external_id VARCHAR(50) UNIQUE,
    university_id INTEGER REFERENCES university(id),
    campus_id INTEGER REFERENCES campus(id),
    category_id INTEGER REFERENCES category(id),
    name VARCHAR(255) NOT NULL,
    official_name VARCHAR(255),
    aliases TEXT,
    description TEXT,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    entrance_latitude DOUBLE PRECISION,
    entrance_longitude DOUBLE PRECISION,
    coordinate_type VARCHAR(50) DEFAULT 'POINT',
    confidence FLOAT DEFAULT 1.0,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    building_code VARCHAR(50),
    floor INTEGER,
    room_number VARCHAR(50),
    google_place_id TEXT,
    google_name TEXT,
    google_address TEXT,
    google_type TEXT,
    verification_status VARCHAR(50) DEFAULT 'UNVERIFIED',
    source TEXT,
    image_url TEXT,
    phone VARCHAR(50),
    email VARCHAR(100),
    opening_hours TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    source_primary TEXT,
    source_secondary TEXT,
    source_map TEXT,
    source_notes TEXT,
    verified_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- SEED DATA - Universities
INSERT INTO university (id, external_id, name, official_name, short_name, city, country, latitude, longitude, default_zoom, description, logo_url, sort_order, campus_area_hectares) VALUES
(1, 'UDSM', 'University of Dar es Salaam', 'University of Dar es Salaam', 'UDSM', 'Dar es Salaam', 'Tanzania', -6.7801, 39.2041, 15.0, 'Hekima ni Uhuru', 'https://upload.wikimedia.org/wikipedia/en/2/2a/University_of_Dar_es_Salaam_Logo.png', 1, 657.0),
(2, 'UDOM', 'University of Dodoma', 'University of Dodoma', 'UDOM', 'Dodoma', 'Tanzania', -6.2033, 35.8000, 14.0, 'Embracing Knowledge', 'https://upload.wikimedia.org/wikipedia/en/1/1b/UDOM_Logo.png', 2, 6000.0),
(3, 'MUST', 'Mbeya University of Science and Technology', 'Mbeya University of Science and Technology', 'MUST', 'Mbeya', 'Tanzania', -8.9328, 33.3980, 15.0, 'Science and Technology for Development', 'https://upload.wikimedia.org/wikipedia/en/a/a2/Mbeya_University_of_Science_and_Technology_Logo.png', 3, NULL),
(4, 'SUA', 'Sokoine University of Agriculture', 'Sokoine University of Agriculture', 'SUA', 'Morogoro', 'Tanzania', -6.8475, 37.6591, 15.0, 'Ardhi ni Hazina', 'https://upload.wikimedia.org/wikipedia/en/3/3d/Sua_logo.png', 4, NULL),
(5, 'MU', 'Mzumbe University', 'Mzumbe University', 'MU', 'Morogoro', 'Tanzania', -6.8167, 37.6667, 15.0, 'Muscente Discimus', 'https://upload.wikimedia.org/wikipedia/en/e/e0/Mzumbe_University_logo.png', 5, 4926.75);

-- Seed Campus
INSERT INTO campus (id, external_id, university_id, name) VALUES
(1, 'UDSM-MLIMANI', 1, 'Mwalimu Julius K. Nyerere Mlimani Campus');

-- BEGIN UDSM SPECIFIC DATA (From Flyway V8)
-- Completely rebuild UDSM Mlimani Campus data with verified coordinates and 13 categories
-- Dataset based on CAMPNAV verified location survey 2026

-- 1. CLEANUP UDSM DATA
DELETE FROM campus_location WHERE university_id = 1;
DELETE FROM category WHERE university_id = 1 OR university_id IS NULL;

-- 2. INSERT 13 CATEGORIES FOR UDSM (University ID 1)
INSERT INTO category (id, university_id, name, slug, icon_name, description, is_active, sort_order) VALUES
(1, 1, 'Academic Colleges & Schools', 'academic', 'school', 'Colleges, Schools, Institutes and Departments', true, 1),
(2, 1, 'Lecture Halls & Classrooms', 'lecture-halls', 'co_present', 'Lecture theatres, classrooms and SR rooms', true, 2),
(3, 1, 'Libraries, Laboratories & Research', 'library', 'menu_book', 'Libraries, Labs and Research centres', true, 3),
(4, 1, 'Student Hostels & Residences', 'hostel', 'hotel', 'Halls of residence and student housing', true, 4),
(5, 1, 'Administrative Offices & Services', 'administration', 'business', 'University administration and support services', true, 5),
(6, 1, 'Health & Medical Services', 'health', 'medical_services', 'UDSM Hospital and health facilities', true, 6),
(7, 1, 'Cafeterias & Dining', 'food', 'restaurant', 'Cafeterias, canteens and dining halls', true, 7),
(8, 1, 'Sports & Recreation', 'sports', 'sports_soccer', 'Sports grounds, courts and gym', true, 8),
(9, 1, 'Banking & Commercial Services', 'banking', 'payments', 'Banks, ATMs and commercial services', true, 9),
(10, 1, 'Security, Parking & Transport', 'security', 'security', 'Police, Parking and Transport facilities', true, 10),
(11, 1, 'Worship, Parks & Community', 'religious', 'account_balance', 'Mosques, Chapels and Gardens', true, 11),
(12, 1, 'Student Organizations & Community', 'community', 'group', 'Student unions and organizations', true, 12),
(13, 1, 'Other Campus Facilities', 'other', 'more_horiz', 'Washrooms and miscellaneous facilities', true, 13);

-- Reset sequence for category
ALTER TABLE category ALTER COLUMN id RESTART WITH 14;

-- 3. INSERT UDSM LOCATIONS WITH VERIFIED COORDINATES

-- CATEGORY 1: ACADEMIC COLLEGES & SCHOOLS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(1, 1, 'CoET', 'College of Engineering and Technology', 'CoET', 'CoET', 'College of Engineering and Technology', -6.780767, 39.206992, 'VERIFIED', true),
(1, 1, 'College of Engineering and Technology (CoET)', 'College of Engineering and Technology', 'CoET', 'CoET', 'College of Engineering and Technology (Alternative Entry)', -6.784035, 39.206389, 'VERIFIED', true),
(1, 1, 'CoNAS', 'College of Natural and Applied Sciences', 'CoNAS', 'CoNAS', 'College of Natural and Applied Sciences', -6.780951, 39.206188, 'VERIFIED', true),
(1, 1, 'CoHU', 'College of Humanities', 'CoHU', 'CoHU', 'College of Humanities', -6.778065, 39.200677, 'VERIFIED', true),
(1, 1, 'CoSS', 'College of Social Sciences', 'CoSS', 'CoSS', 'College of Social Sciences', -6.780096, 39.203426, 'VERIFIED', true),
(1, 1, 'UDBS', 'University of Dar es Salaam Business School', 'UDBS', 'UDBS', 'University of Dar es Salaam Business School', -6.780072, 39.202262, 'VERIFIED', true),
(1, 1, 'School of Education (UDSOE)', 'School of Education', 'UDSOE', 'UDSOE', 'School of Education', -6.778734, 39.200465, 'VERIFIED', true),
(1, 1, 'IDS', 'Institute of Development Studies', 'IDS', 'IDS', 'Institute of Development Studies', -6.779038, 39.203880, 'VERIFIED', true),
(1, 1, 'SoMG', 'School of Mines and Geosciences', 'SoMG', 'SoMG', 'School of Mines and Geosciences', -6.780962, 39.206096, 'VERIFIED', true),
(1, 1, 'CoAF', 'College of Agricultural Sciences and Food Technology', 'CoAF', 'CoAF', 'College of Agricultural Sciences and Food Technology', -6.783493, 39.207590, 'VERIFIED', true),
(1, 1, 'Department of Computer Science and Engineering', 'Department of Computer Science and Engineering', 'CSE', 'CSE', 'Department of Computer Science and Engineering', -6.781144, 39.203462, 'VERIFIED', true),
(1, 1, 'Mining Department Building', 'Mining Department Building', 'Mining', 'Mining', 'Mining Department Building', -6.783262, 39.207209, 'VERIFIED', true),
(1, 1, 'Chemistry Department', 'Chemistry Department', 'Chemistry', 'Chemistry', 'Chemistry Department', -6.780909, 39.204274, 'VERIFIED', true),
(1, 1, 'Physics Department', 'Physics Department', 'Physics', 'Physics', 'Physics Department', -6.780789, 39.203695, 'VERIFIED', true),
(1, 1, 'Zoology & Wildlife Conservation Department', 'Zoology & Wildlife Conservation Department', 'Zoology', 'Zoology', 'Zoology & Wildlife Conservation Department', -6.780987, 39.204796, 'VERIFIED', true),
(1, 1, 'Department of Archaeology', 'Department of Archaeology', 'Archaeology', 'Archaeology', 'Department of Archaeology', -6.777939, 39.200474, 'VERIFIED', true),
(1, 1, 'Department of History', 'Department of History', 'History', 'History', 'Department of History', -6.780281, 39.203581, 'VERIFIED', true),
(1, 1, 'Department of History (Alt Entry)', 'Department of History', 'History', 'History', 'Department of History (Alternative Entry)', -6.780001, 39.203686, 'VERIFIED', true),
(1, 1, 'Department of Foreign Languages and Linguistics', 'Department of Foreign Languages and Linguistics', 'Linguistics', 'Linguistics', 'Department of Foreign Languages and Linguistics', -6.780178, 39.203690, 'VERIFIED', true),
(1, 1, 'Confucius Institute UDSM', 'Confucius Institute UDSM', 'Confucius', 'Confucius', 'Confucius Institute UDSM', -6.781422, 39.201422, 'VERIFIED', true),
(1, 1, 'Taasisi ya Kiswahili – TUKI', 'Taasisi ya Kiswahili – TUKI', 'TUKI', 'TUKI', 'Taasisi ya Kiswahili – TUKI', -6.779202, 39.204338, 'VERIFIED', true),
(1, 1, 'Institute of Resource Assessment (IRA)', 'Institute of Resource Assessment', 'IRA', 'IRA', 'Institute of Resource Assessment (IRA)', -6.779576, 39.203965, 'VERIFIED', true),
(1, 1, 'IRA Building (Institute of Geography)', 'IRA Building', 'Geography', 'Geography', 'IRA Building (Institute of Geography)', -6.779511, 39.203719, 'VERIFIED', true),
(1, 1, 'Coict Tele Education Centre', 'Coict Tele Education Centre', 'Tele Education', 'Tele-Ed', 'Coict Tele Education Centre', -6.781249, 39.204434, 'VERIFIED', true),
(1, 1, 'Transportation Building (TGES)', 'Transportation Building', 'TGES', 'TGES', 'Transportation Building (TGES)', -6.781002, 39.207700, 'VERIFIED', true),
(1, 1, 'CoET Block A', 'CoET Block A', 'Block A', 'Block A', 'CoET Block A', -6.780934, 39.206854, 'VERIFIED', true),
(1, 1, 'Block Q', 'Block Q', 'Block Q', 'Block Q', 'Block Q', -6.783515, 39.206983, 'VERIFIED', true);

-- CATEGORY 2: LECTURE HALLS & CLASSROOMS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(1, 2, 'Nkrumah Hall', 'Nkrumah Hall', 'Nkrumah', 'Nkrumah', 'Nkrumah Hall', -6.780937, 39.204989, 'VERIFIED', true),
(1, 2, 'Theatre 1', 'Theatre 1', 'T1', 'T1', 'Theatre 1', -6.780214, 39.206274, 'VERIFIED', true),
(1, 2, 'Theatre Two', 'Theatre Two', 'T2', 'T2', 'Theatre Two', -6.780292, 39.206312, 'VERIFIED', true),
(1, 2, 'A21 Theater Room', 'A21 Theater Room', 'A21', 'A21', 'A21 Theater Room', -6.780912, 39.206982, 'VERIFIED', true),
(1, 2, 'Yombo 2', 'Yombo 2', 'Yombo 2', 'Y2', 'Yombo 2', -6.777003, 39.202488, 'VERIFIED', true),
(1, 2, 'Yombo 4', 'Yombo 4', 'Yombo 4', 'Y4', 'Yombo 4', -6.777500, 39.201557, 'VERIFIED', true),
(1, 2, 'Yombo Five', 'Yombo Five', 'Yombo 5', 'Y5', 'Yombo Five', -6.778032, 39.201462, 'VERIFIED', true),
(1, 2, 'Yombo Theatres', 'Yombo Theatres', 'Yombo', 'Yombo', 'Yombo Theatres', -6.777800, 39.201477, 'VERIFIED', true),
(1, 2, 'New Yombo Lecture Theatre 5', 'New Yombo Lecture Theatre 5', 'New Yombo 5', 'NY5', 'New Yombo Lecture Theatre 5', -6.776240, 39.201574, 'VERIFIED', true),
(1, 2, 'Multipurpose Hall', 'Multipurpose Hall', 'Multipurpose', 'MPH', 'Multipurpose Hall', -6.776872, 39.209036, 'VERIFIED', true),
(1, 2, 'New Library Auditorium Hall', 'New Library Auditorium Hall', 'Library Auditorium', 'NLA', 'New Library Auditorium Hall', -6.778309, 39.202833, 'VERIFIED', true),
(1, 2, 'SR 15 UDSM', 'SR 15 UDSM', 'SR 15', 'SR15', 'SR 15 UDSM', -6.780368, 39.204114, 'VERIFIED', true),
(1, 2, 'SR 16 UDSM', 'SR 16 UDSM', 'SR 16', 'SR16', 'SR 16 UDSM', -6.780322, 39.204187, 'VERIFIED', true),
(1, 2, 'SR 10 Ground Floor UDSM', 'SR 10 Ground Floor UDSM', 'SR 10', 'SR10', 'SR 10 Ground Floor UDSM', -6.780322, 39.204187, 'VERIFIED', true),
(1, 2, 'Class B Class Room UDSM', 'Class B Class Room UDSM', 'Class B', 'ClassB', 'Class B Class Room UDSM', -6.780305, 39.203809, 'VERIFIED', true),
(1, 2, 'CASS A Room 2nd Floor UDSM', 'CASS A Room 2nd Floor UDSM', 'CASS A', 'CASS A', 'CASS A Room 2nd Floor UDSM', -6.780226, 39.203834, 'VERIFIED', true),
(1, 2, 'Academic Bridge', 'Academic Bridge', 'Bridge', 'Bridge', 'Academic Bridge', -6.778147, 39.206547, 'VERIFIED', true),
(1, 2, 'DARUSO Bridge', 'DARUSO Bridge', 'DARUSO Bridge', 'DARUSO Bridge', 'DARUSO Bridge', -6.778432, 39.206895, 'VERIFIED', true),
(1, 2, 'SR6 Ground Floor UDSM', 'SR6 Ground Floor UDSM', 'SR 6', 'SR6', 'SR6 Ground Floor UDSM', -6.779943, 39.204280, 'VERIFIED', true),
(1, 2, 'R 1 UDSM Ground Floor', 'R 1 UDSM Ground Floor', 'R 1', 'R1', 'R 1 UDSM Ground Floor', -6.779714, 39.204288, 'VERIFIED', true),
(1, 2, 'Fiscal House Building', 'Fiscal House Building', 'Fiscal House', 'Fiscal', 'Fiscal House Building', -6.779550, 39.203719, 'VERIFIED', true),
(1, 2, 'Science Complex', 'Science Complex', 'Science', 'Science', 'Science Complex', -6.783080, 39.201912, 'VERIFIED', true);

-- CATEGORY 3: LIBRARIES, LABORATORIES & RESEARCH
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(1, 3, 'Wilbert Chagula Library', 'Wilbert Chagula Library', 'Library', 'Library', 'Wilbert Chagula Library', -6.782910, 39.204871, 'VERIFIED', true),
(1, 3, 'UDSM Old Library', 'UDSM Old Library', 'Old Library', 'OldLib', 'UDSM Old Library', -6.780033, 39.205454, 'VERIFIED', true),
(1, 3, 'UDSM New Library', 'UDSM New Library', 'New Library', 'NewLib', 'UDSM New Library', -6.778894, 39.202328, 'VERIFIED', true),
(1, 3, 'Vimbweta New Library', 'Vimbweta New Library', 'Vimbweta', 'Vimbweta', 'Vimbweta New Library', -6.779148, 39.203390, 'VERIFIED', true),
(1, 3, 'Mdigrii Vimbweta', 'Mdigrii Vimbweta', 'Mdigrii', 'Mdigrii', 'Mdigrii Vimbweta', -6.780651, 39.203693, 'VERIFIED', true),
(1, 3, 'TCLAB', 'TCLAB', 'TCLAB', 'TCLAB', 'TCLAB', -6.781153, 39.203502, 'VERIFIED', true),
(1, 3, 'Highway Laboratory', 'Highway Laboratory', 'Highway Lab', 'Highway', 'Highway Laboratory', -6.781736, 39.207748, 'VERIFIED', true),
(1, 3, 'Dr. E. Ndiralema (Water Quality)', 'Dr. E. Ndiralema (Water Quality)', 'Water Quality', 'Water', 'Dr. E. Ndiralema (Water Quality)', -6.781671, 39.207123, 'VERIFIED', true),
(1, 3, 'Maabara / Laboratory', 'Maabara / Laboratory', 'Lab', 'Lab', 'Maabara / Laboratory', -6.778949, 39.208231, 'VERIFIED', true),
(1, 3, 'UDSM GIS Laboratory', 'UDSM GIS Laboratory', 'GIS Lab', 'GIS', 'UDSM GIS Laboratory', -6.779725, 39.204178, 'VERIFIED', true),
(1, 3, 'Population Studies & Research Centre', 'Population Studies & Research Centre', 'PSRC', 'PSRC', 'Population Studies & Research Centre', -6.780691, 39.205394, 'VERIFIED', true),
(1, 3, 'UDSM Zoology Museum', 'UDSM Zoology Museum', 'Zoology Museum', 'Museum', 'UDSM Zoology Museum', -6.782680, 39.201712, 'VERIFIED', true),
(1, 3, 'UCC - HQ', 'UCC - HQ', 'UCC', 'UCC', 'UCC - HQ', -6.781578, 39.202978, 'VERIFIED', true),
(1, 3, 'TDTC', 'TDTC', 'TDTC', 'TDTC', 'TDTC', -6.784308, 39.206288, 'VERIFIED', true),
(1, 3, 'BICO', 'BICO', 'BICO', 'BICO', 'BICO', -6.783762, 39.206873, 'VERIFIED', true),
(1, 3, 'BICO GARAGE', 'BICO GARAGE', 'BICO Garage', 'Garage', 'BICO GARAGE', -6.780102, 39.200977, 'VERIFIED', true),
(1, 3, 'CodeNest Labs', 'CodeNest Labs', 'CodeNest', 'CodeNest', 'IT Training/Incubation', -6.770033, 39.214424, 'NEEDS_REVIEW', true);

-- CATEGORY 4: STUDENT HOSTELS & RESIDENCES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(1, 4, 'UDSM International Flats', 'UDSM International Flats', 'Int Flats', 'IntFlats', 'UDSM International Flats', -6.777490, 39.208873, 'VERIFIED', true),
(1, 4, 'UDSM Research Flats', 'UDSM Research Flats', 'Res Flats', 'ResFlats', 'UDSM Research Flats', -6.783176, 39.203422, 'VERIFIED', true),
(1, 4, 'Hall 6 Block C', 'Hall 6 Block C', 'H6C', 'H6C', 'Hall 6 Block C', -6.775817, 39.202499, 'VERIFIED', true),
(1, 4, 'Hall VI Block A', 'Hall VI Block A', 'H6A', 'H6A', 'Hall VI Block A', -6.775841, 39.202895, 'VERIFIED', true),
(1, 4, 'Water Resources Hostel', 'Water Resources Hostel', 'Water Hostel', 'WaterH', 'Water Resources Hostel', -6.775541, 39.202719, 'VERIFIED', true),
(1, 4, 'Hall 3 Residency', 'Hall 3 Residency', 'Hall 3', 'H3', 'Hall 3 Residency', -6.775372, 39.205991, 'VERIFIED', true),
(1, 4, 'Julie Manning Hall Residency', 'Julie Manning Hall Residency', 'Julie Manning', 'JMH', 'Julie Manning Hall Residency', -6.775060, 39.207313, 'VERIFIED', true),
(1, 4, 'Hall 4', 'Hall 4', 'Hall 4', 'H4', 'Hall 4', -6.776344, 39.205845, 'VERIFIED', true),
(1, 4, 'Hall 5', 'Hall 5', 'Hall 5', 'H5', 'Hall 5', -6.776308, 39.206993, 'VERIFIED', true),
(1, 4, 'Hall 2', 'Hall 2', 'Hall 2', 'H2', 'Hall 2', -6.776452, 39.207616, 'VERIFIED', true),
(1, 4, 'Magufuli Hostels Rd', 'Magufuli Hostels Rd', 'Hostel Rd', 'HostelRd', 'Magufuli Hostels Rd', -6.780293, 39.210676, 'VERIFIED', true),
(1, 4, 'Magufuli Hostels', 'Magufuli Hostels', 'Magufuli', 'Magufuli', 'Magufuli Hostels', -6.781830, 39.213482, 'VERIFIED', true),
(1, 4, 'UDSM Flats Survey', 'UDSM Flats Survey', 'Survey Flats', 'Survey', 'UDSM Flats Survey', -6.767869, 39.215281, 'NEEDS_REVIEW', true),
(1, 4, 'Mlimani Villas', 'Mlimani Villas', 'Villas', 'Villas', 'Mlimani Villas', -6.770723, 39.219023, 'NEEDS_REVIEW', true);

-- CATEGORY 5: ADMINISTRATIVE OFFICES & SERVICES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(1, 5, 'Administration Block', 'Administration Block', 'Admin', 'Admin', 'Administration Block', -6.780563, 39.203300, 'VERIFIED', true),
(1, 5, 'Samora', 'Samora', 'Samora', 'Samora', 'Samora Building', -6.780597, 39.202956, 'VERIFIED', true),
(1, 5, 'ARIS Office', 'ARIS Office', 'ARIS', 'ARIS', 'ARIS Office', -6.780153, 39.205409, 'VERIFIED', true),
(1, 5, 'Dean of Students (DOS Office)', 'Dean of Students (DOS Office)', 'DOS', 'DOS', 'Dean of Students (DOS Office)', -6.776803, 39.202140, 'VERIFIED', true),
(1, 5, 'DARUSO Office', 'DARUSO Office', 'DARUSO', 'DARUSO', 'DARUSO Office', -6.776553, 39.202580, 'VERIFIED', true),
(1, 5, 'Estate Department', 'Estate Department', 'Estate', 'Estate', 'Estate Department', -6.780212, 39.214415, 'VERIFIED', true),
(1, 5, 'UDSM PMU Office', 'UDSM PMU Office', 'PMU', 'PMU', 'UDSM PMU Office', -6.778651, 39.206377, 'VERIFIED', true),
(1, 5, 'UDSM Post Office', 'UDSM Post Office', 'Post Office', 'Post', 'UDSM Post Office', -6.778803, 39.206383, 'VERIFIED', true);

-- CATEGORY 6: HEALTH & MEDICAL SERVICES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(1, 6, 'Dispensary', 'Dispensary', 'Dispensary', 'Disp', 'Dispensary', -6.778661, 39.208273, 'VERIFIED', true),
(1, 6, 'UDSM Hospital', 'UDSM Hospital', 'Hospital', 'Hosp', 'UDSM Hospital', -6.778731, 39.208280, 'VERIFIED', true),
(1, 6, 'Pharmacy (Dirisha la Dawa)', 'Pharmacy (Dirisha la Dawa)', 'Pharmacy', 'Pharm', 'Pharmacy (Dirisha la Dawa)', -6.778980, 39.208144, 'VERIFIED', true),
(1, 6, 'Chuo Kikuu Health Centre', 'Chuo Kikuu Health Centre', 'Health Centre', 'HC', 'Chuo Kikuu Health Centre', -6.779160, 39.208153, 'VERIFIED', true),
(1, 6, 'Psychosocial Welfare', 'Psychosocial Welfare', 'Psychosocial', 'Psych', 'Psychosocial Welfare', -6.775797, 39.200775, 'VERIFIED', true),
(1, 6, 'Psychotherapy Help Organisation', 'Psychotherapy Help Organisation', 'Psychotherapy', 'PHO', 'Psychotherapy Help Organisation', -6.776149, 39.200792, 'VERIFIED', true);

-- CATEGORY 7: CAFETERIAS & DINING
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(1, 7, 'Utawala Cafeteria', 'Utawala Cafeteria', 'Admin Cafe', 'AdminCafe', 'Utawala Cafeteria', -6.780494, 39.203499, 'VERIFIED', true),
(1, 7, 'Yombo Cafeteria', 'Yombo Cafeteria', 'Yombo Cafe', 'YomboCafe', 'Yombo Cafeteria', -6.776554, 39.201322, 'VERIFIED', true),
(1, 7, 'UDSM Cafeteria 1', 'UDSM Cafeteria 1', 'Cafe 1', 'Cafe1', 'UDSM Cafeteria 1', -6.777792, 39.207792, 'VERIFIED', true),
(1, 7, 'Magufuli Hostels Cafeteria', 'Magufuli Hostels Cafeteria', 'Magufuli Cafe', 'MagufuliCafe', 'Magufuli Hostels Cafeteria', -6.780765, 39.212133, 'VERIFIED', true),
(1, 7, 'Mama Lishe Cafeteria', 'Mama Lishe Cafeteria', 'Mama Lishe', 'MamaLishe', 'Mama Lishe Cafeteria', -6.780925, 39.201253, 'VERIFIED', true),
(1, 7, 'DARUSO Bar', 'DARUSO Bar', 'DARUSO Bar', 'DARUSOBar', 'DARUSO Bar', -6.778975, 39.206909, 'VERIFIED', true),
(1, 7, 'Villa Juice Point', 'Villa Juice Point', 'Villa Juice', 'VillaJuice', 'Villa Juice Point', -6.778333, 39.207503, 'VERIFIED', true),
(1, 7, 'Hall 4 Women and Men Salon', 'Hall 4 Women and Men Salon', 'Hall 4 Salon', 'H4Salon', 'Hall 4 Women and Men Salon', -6.776040, 39.205558, 'VERIFIED', true);

-- CATEGORY 8: SPORTS & RECREATION
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(1, 8, 'Scout Ground', 'Scout Ground', 'Scout', 'Scout', 'Scout Ground', -6.779442, 39.208672, 'VERIFIED', true),
(1, 8, 'UDSM Volleyball Court', 'UDSM Volleyball Court', 'Volleyball', 'Volleyball', 'UDSM Volleyball Court', -6.779486, 39.209112, 'VERIFIED', true),
(1, 8, 'UDSM Swimming Pool', 'UDSM Swimming Pool', 'Swimming Pool', 'Pool', 'UDSM Swimming Pool', -6.779645, 39.208131, 'VERIFIED', true),
(1, 8, 'Tennis Ground', 'Tennis Ground', 'Tennis', 'Tennis', 'Tennis Ground', -6.779820, 39.208672, 'VERIFIED', true),
(1, 8, 'UDSM Netball Playground', 'UDSM Netball Playground', 'Netball', 'Netball', 'UDSM Netball Playground', -6.779895, 39.209151, 'VERIFIED', true),
(1, 8, 'Basketball Court', 'Basketball Court', 'Basketball', 'Basketball', 'Basketball Court', -6.779905, 39.209628, 'VERIFIED', true),
(1, 8, 'Sports Ground', 'Sports Ground', 'Sports', 'Sports', 'Sports Ground', -6.781645, 39.209599, 'VERIFIED', true),
(1, 8, 'Mlimani Football Ground', 'Mlimani Football Ground', 'Football', 'Football', 'Mlimani Football Ground', -6.782184, 39.210979, 'VERIFIED', true),
(1, 8, 'UDSM Cricket Playground', 'UDSM Cricket Playground', 'Cricket', 'Cricket', 'UDSM Cricket Playground', -6.783591, 39.209399, 'VERIFIED', true),
(1, 8, 'Tanzania Cricket Arena', 'Tanzania Cricket Arena', 'Cricket Arena', 'Arena', 'Tanzania Cricket Arena', -6.784289, 39.209938, 'VERIFIED', true),
(1, 8, 'UDSM Gym', 'UDSM Gym', 'Gym', 'Gym', 'UDSM Gym', -6.779065, 39.206975, 'VERIFIED', true);

-- CATEGORY 9: BANKING & COMMERCIAL SERVICES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(1, 9, 'NBC / Atm / UDSM Branch', 'NBC / Atm / UDSM Branch', 'NBC', 'NBC', 'NBC / Atm / UDSM Branch', -6.781268, 39.202786, 'VERIFIED', true),
(1, 9, 'NBC Mlimani Branch', 'NBC Mlimani Branch', 'NBC Mlimani', 'NBCM', 'NBC Mlimani Branch', -6.781313, 39.202444, 'VERIFIED', true),
(1, 9, 'CRDB Bank (UDSM Branch)', 'CRDB Bank (UDSM Branch)', 'CRDB', 'CRDB', 'CRDB Bank (UDSM Branch)', -6.780514, 39.201474, 'VERIFIED', true),
(1, 9, 'NMB UDSM Branch', 'NMB UDSM Branch', 'NMB', 'NMB', 'NMB UDSM Branch', -6.779983, 39.202422, 'VERIFIED', true),
(1, 9, 'NMB ATM', 'NMB ATM', 'NMB ATM', 'NMBATM', 'NMB ATM', -6.778691, 39.206905, 'VERIFIED', true),
(1, 9, 'CRDB ATM', 'CRDB ATM', 'CRDB ATM', 'CRDBATM', 'CRDB ATM', -6.778806, 39.206349, 'VERIFIED', true),
(1, 9, 'UDSM SACCOS', 'UDSM SACCOS', 'SACCOS', 'SACCOS', 'UDSM SACCOS', -6.780682, 39.205622, 'VERIFIED', true),
(1, 9, 'DUP', 'DUP', 'DUP', 'DUP', 'Dar es Salaam University Press', -6.780652, 39.205702, 'VERIFIED', true),
(1, 9, 'TTCL Shop UDSM', 'TTCL Shop UDSM', 'TTCL', 'TTCL', 'TTCL Shop UDSM', -6.781331, 39.202432, 'VERIFIED', true),
(1, 9, 'Elite University Bookstore', 'Elite University Bookstore', 'Bookstore', 'Bookstore', 'Elite University Bookstore', -6.778607, 39.206445, 'VERIFIED', true),
(1, 9, 'Old Library Stationery', 'Old Library Stationery', 'Stationery', 'Stat', 'Old Library Stationery', -6.780309, 39.205104, 'VERIFIED', true),
(1, 9, 'Yetu Stationery', 'Yetu Stationery', 'Yetu', 'Yetu', 'Yetu Stationery', -6.780531, 39.191645, 'VERIFIED', true), -- Note: original had 39.201645. Wait, prompt says 39.201645. I'll use prompt value.
(1, 9, 'DARUSO Stationery', 'DARUSO Stationery', 'DARUSO Stat', 'DARUSOStat', 'DARUSO Stationery', -6.778689, 39.206985, 'VERIFIED', true),
(1, 9, 'Stationaries', 'Stationaries', 'Stationeries', 'Stat2', 'Stationaries', -6.780539, 39.205329, 'VERIFIED', true),
(1, 9, 'Hill Park Super Market', 'Hill Park Super Market', 'Hill Park', 'HillPark', 'Hill Park Super Market', -6.781117, 39.201949, 'VERIFIED', true),
(1, 9, 'Katuro Shop', 'Katuro Shop', 'Katuro', 'Katuro', 'Katuro Shop', -6.781231, 39.202421, 'VERIFIED', true),
(1, 9, 'UDSM Business Park', 'UDSM Business Park', 'Business Park', 'BizPark', 'UDSM Business Park', -6.780282, 39.212429, 'NEEDS_REVIEW', true),
(1, 9, 'Western Union', 'Western Union', 'Western Union', 'WU', 'Western Union', -6.771696, 39.217197, 'NEEDS_REVIEW', true),
(1, 9, 'Mlimani City Shopping Mall', 'Mlimani City Shopping Mall', 'Mlimani City', 'MlimaniCity', 'Mlimani City Shopping Mall', -6.772178, 39.219680, 'NEEDS_REVIEW', true);

-- Fix Yetu Stationery longitude
UPDATE campus_location SET longitude = 39.201645 WHERE name = 'Yetu Stationery';

-- CATEGORY 10: SECURITY, PARKING & TRANSPORT
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(1, 10, 'UDSM Auxiliary Police Post', 'UDSM Auxiliary Police Post', 'Police Post', 'Police1', 'UDSM Auxiliary Police Post', -6.780684, 39.202458, 'VERIFIED', true),
(1, 10, 'Kituo Cha Polisi UDSM', 'Kituo Cha Polisi UDSM', 'Police Station', 'Police2', 'Kituo Cha Polisi UDSM', -6.781141, 39.202000, 'VERIFIED', true),
(1, 10, 'UDSM Public Parking 4', 'UDSM Public Parking 4', 'Parking 4', 'P4', 'UDSM Public Parking 4', -6.782658, 39.202507, 'VERIFIED', true),
(1, 10, 'CoET Block O Parking Lot', 'CoET Block O Parking Lot', 'CoET Parking O', 'PCoET_O', 'CoET Block O Parking Lot', -6.782804, 39.206935, 'VERIFIED', true),
(1, 10, 'CoET Parking', 'CoET Parking', 'CoET Parking', 'PCoET', 'CoET Parking', -6.780340, 39.207329, 'VERIFIED', true),
(1, 10, 'NMB Parking', 'NMB Parking', 'NMB Parking', 'PNMB', 'NMB Parking', -6.783517, 39.207550, 'VERIFIED', true),
(1, 10, 'Staff Cars Parking', 'Staff Cars Parking', 'Staff Parking', 'PStaff', 'Staff Cars Parking', -6.781054, 39.204194, 'VERIFIED', true),
(1, 10, 'Utawala Staff Parking', 'Utawala Staff Parking', 'Admin Parking', 'PAdmin', 'Utawala Staff Parking', -6.780727, 39.202999, 'VERIFIED', true),
(1, 10, 'Nkrumah Hall Parking', 'Nkrumah Hall Parking', 'Nkrumah Parking', 'PNkrumah', 'Nkrumah Hall Parking', -6.780592, 39.204319, 'VERIFIED', true),
(1, 10, 'Library Parking', 'Library Parking', 'Library Parking', 'PLib', 'Library Parking', -6.780345, 39.204998, 'VERIFIED', true),
(1, 10, 'Parking Lot', 'Parking Lot', 'Parking Lot', 'PLot', 'Parking Lot', -6.779647, 39.207587, 'VERIFIED', true),
(1, 10, 'Parking Lot (Sports/Shuttle Area)', 'Parking Lot (Sports/Shuttle Area)', 'Shuttle Parking', 'PShuttle', 'Parking Lot (Sports/Shuttle Area)', -6.779635, 39.207601, 'VERIFIED', true),
(1, 10, 'Swimming Car Parking', 'Swimming Car Parking', 'Swimming Parking', 'PPool', 'Swimming Car Parking', -6.780077, 39.208090, 'VERIFIED', true),
(1, 10, 'CRDB / UDBS Parking Lot', 'CRDB / UDBS Parking Lot', 'CRDB Parking', 'PCRDB', 'CRDB / UDBS Parking Lot', -6.780608, 39.201329, 'VERIFIED', true),
(1, 10, 'UDSM Auxiliary Police Car Parking', 'UDSM Auxiliary Police Car Parking', 'Police Parking', 'PPolice', 'UDSM Auxiliary Police Car Parking', -6.780377, 39.202720, 'VERIFIED', true),
(1, 10, 'CoSS Car Parking', 'CoSS Car Parking', 'CoSS Parking', 'PCoSS', 'CoSS Car Parking', -6.779031, 39.204031, 'VERIFIED', true),
(1, 10, 'UDSM Public Parking 2', 'UDSM Public Parking 2', 'Parking 2', 'P2', 'UDSM Public Parking 2', -6.780923, 39.201427, 'VERIFIED', true),
(1, 10, 'Cafeteria Car Parking', 'Cafeteria Car Parking', 'Cafe Parking', 'PCafe', 'Cafeteria Car Parking', -6.778890, 39.206609, 'VERIFIED', true),
(1, 10, 'Yombo Parking', 'Yombo Parking', 'Yombo Parking', 'PYombo', 'Yombo Parking', -6.776779, 39.201746, 'VERIFIED', true),
(1, 10, 'Shuttle Point UDSM', 'Shuttle Point UDSM', 'Shuttle Point', 'Shuttle', 'Shuttle Point UDSM', -6.779651, 39.207423, 'VERIFIED', true),
(1, 10, 'Kituo Cha Basi Cafeteria', 'Kituo Cha Basi Cafeteria', 'Bus Stop Cafe', 'BusStop1', 'Kituo Cha Basi Cafeteria', -6.777977, 39.207799, 'VERIFIED', true),
(1, 10, 'New Library Bus Stop', 'New Library Bus Stop', 'Bus Stop Library', 'BusStop2', 'New Library Bus Stop', -6.778779, 39.203774, 'VERIFIED', true),
(1, 10, 'Kituo Cha Basi Cafeteria Bus Stop', 'Kituo Cha Basi Cafeteria Bus Stop', 'Bus Stop Cafe 2', 'BusStop3', 'Kituo Cha Basi Cafeteria Bus Stop', -6.776892, 39.208635, 'VERIFIED', true),
(1, 10, 'TPDC CNG STATION MLIMANI', 'TPDC CNG STATION MLIMANI', 'CNG Station', 'CNG', 'TPDC CNG STATION MLIMANI', -6.784022, 39.212813, 'VERIFIED', true);

-- CATEGORY 11: WORSHIP, PARKS & COMMUNITY
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(1, 11, 'UDSM Masjid', 'UDSM Masjid', 'Mosque', 'Mosque', 'UDSM Masjid', -6.776995, 39.204820, 'VERIFIED', true),
(1, 11, 'UDSM Chapel', 'UDSM Chapel', 'Chapel', 'Chapel', 'UDSM Chapel', -6.775919, 39.204942, 'VERIFIED', true),
(1, 11, 'EAGT UDSM Mlimani', 'EAGT UDSM Mlimani', 'EAGT', 'EAGT', 'EAGT UDSM Mlimani', -6.777071, 39.202410, 'VERIFIED', true),
(1, 11, 'UDSM LOVE ZONE (GARDEN)', 'UDSM LOVE ZONE (GARDEN)', 'Love Zone', 'Garden', 'UDSM LOVE ZONE (GARDEN)', -6.775490, 39.208672, 'VERIFIED', true),
(1, 11, 'Hill Park', 'Hill Park', 'Hill Park', 'HillPark', 'Hill Park', -6.780944, 39.201683, 'VERIFIED', true),
(1, 11, 'Arts (Fine and Performing Arts)', 'Arts (Fine and Performing Arts)', 'Fine Arts', 'Arts', 'Arts (Fine and Performing Arts)', -6.779674, 39.201425, 'VERIFIED', true),
(1, 11, 'Mlimani Demonstration', 'Mlimani Demonstration', 'Mlimani Demo', 'Demo', 'Mlimani Demonstration', -6.776350, 39.212333, 'VERIFIED', true),
(1, 11, 'Vidudu Nursery School', 'Vidudu Nursery School', 'Vidudu', 'Vidudu', 'Vidudu Nursery School', -6.776001, 39.211903, 'VERIFIED', true);

-- CATEGORY 12: STUDENT ORGANIZATIONS & COMMUNITY
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(1, 12, 'UDASA Club', 'UDASA Club', 'UDASA', 'UDASA', 'UDASA Club', -6.773290, 39.211174, 'VERIFIED', true),
(1, 12, 'UDASA Bridge', 'UDASA Bridge', 'UDASA Bridge', 'UDASAB', 'UDASA Bridge', -6.772685, 39.213174, 'VERIFIED', true),
(1, 12, 'UN Chapter at UDSM', 'UN Chapter at UDSM', 'UN Chapter', 'UN', 'UN Chapter at UDSM', -6.780816, 39.203329, 'VERIFIED', true),
(1, 12, 'AIESEC', 'AIESEC', 'AIESEC', 'AIESEC', 'AIESEC', -6.776543, 39.207731, 'VERIFIED', true),
(1, 12, 'SINE SEC GROUP', 'SINE SEC GROUP', 'SINE', 'SINE', 'SINE SEC GROUP', -6.776077, 39.202875, 'VERIFIED', true),
(1, 12, 'Tanzania Education & Research Network', 'Tanzania Education & Research Network', 'TERNET', 'TERNET', 'Tanzania Education & Research Network', -6.775944, 39.203591, 'VERIFIED', true),
(1, 12, 'UDIAA', 'UDIAA', 'UDIAA', 'UDIAA', 'UDIAA', -6.780160, 39.202408, 'VERIFIED', true),
(1, 12, 'Silabu – The Social Learning App', 'Silabu – The Social Learning App', 'Silabu', 'Silabu', 'Silabu – The Social Learning App', -6.779070, 39.206939, 'VERIFIED', true),
(1, 12, 'Hall One Internet Cafe', 'Hall One Internet Cafe', 'Internet Cafe', 'Cafe', 'Hall One Internet Cafe', -6.777782, 39.206721, 'VERIFIED', true);

-- CATEGORY 13: OTHER CAMPUS FACILITIES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(1, 13, 'Wash Rooms / Toilets (Msalani)', 'Wash Rooms / Toilets (Msalani)', 'Toilets', 'Toilet1', 'Wash Rooms / Toilets (Msalani)', -6.779176, 39.205018, 'VERIFIED', true),
(1, 13, 'Cafeteria Toilets / Msalani', 'Cafeteria Toilets / Msalani', 'Toilets', 'Toilet2', 'Cafeteria Toilets / Msalani', -6.779022, 39.206954, 'VERIFIED', true);

-- Seed UDOM Campus
INSERT INTO campus (id, external_id, university_id, name) VALUES
(2, 'UDOM-MAIN', 2, 'University of Dodoma Main Campus') ON CONFLICT DO NOTHING;

-- BEGIN UDOM SPECIFIC DATA (From Flyway V12)
DELETE FROM campus_location WHERE university_id = 2;
DELETE FROM category WHERE university_id = 2;

-- INSERT 11 CATEGORIES FOR UDOM (University ID 2)
INSERT INTO category (id, university_id, name, slug, icon_name, description, is_active, sort_order) VALUES
(14, 2, 'Academic Facilities', 'udom-academic', 'school', 'Colleges, Libraries, Classrooms, Labs & Academic Blocks', true, 1),
(15, 2, 'Administrative & Official Buildings', 'udom-administration', 'business', 'Central Administration and Official Offices', true, 2),
(16, 2, 'Student Accommodation & Hostels', 'udom-hostel', 'hotel', 'Hostels, Blocks and Student Residences', true, 3),
(17, 2, 'Commercial, Shopping & Financial Services', 'udom-banking', 'payments', 'Banks, ATMs, Shops, Malls & Markets', true, 4),
(18, 2, 'Dining & Cafeterias', 'udom-food', 'restaurant', 'Cafeterias, Coffee Shops and Restaurants', true, 5),
(19, 2, 'Medical & Health Facilities', 'udom-health', 'medical_services', 'Hospitals, Clinics and Allied Health Units', true, 6),
(20, 2, 'Sports, Recreation & Social Facilities', 'udom-sports', 'sports_soccer', 'Basketball, Football, Netball courts and Entertainment', true, 7),
(21, 2, 'Places of Worship', 'udom-religious', 'account_balance', 'Mosques, Churches and Chapels', true, 8),
(22, 2, 'Infrastructure, Landmarks & Transport', 'udom-infrastructure', 'place', 'Bus stops, Viewpoints, Water tanks & Landmarks', true, 9),
(23, 2, 'Parking Areas', 'udom-parking', 'local_parking', 'Car, Bus & Campus Parking Areas', true, 10),
(24, 2, 'Schools & Primary Education', 'udom-schools', 'school', 'Primary and Sample Schools', true, 11);

-- INSERT UDOM LOCATIONS WITH VERIFIED COORDINATES

-- CATEGORY 14: ACADEMIC FACILITIES (LIBRARIES, CLASSROOMS, LABS & COLLEGES)
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 14, 'CHSS Master''s Room', 'CHSS Master''s Room', 'CHSS Masters', 'CHSS-MR', 'CHSS Master''s Room', -6.210004, 35.798344, 'VERIFIED', true),
(2, 14, 'Social Seminar Room AB2', 'Social Seminar Room AB2', 'Social AB2', 'SR-AB2', 'Social Seminar Room AB2', -6.210688, 35.797395, 'VERIFIED', true),
(2, 14, 'CHSS Theatre 2', 'CHSS Theatre 2', 'Theatre 2', 'CHSS-T2', 'CHSS Lecture Theatre 2', -6.210921, 35.796725, 'VERIFIED', true),
(2, 14, 'CHSS Theatre 1', 'CHSS Theatre 1', 'Theatre 1', 'CHSS-T1', 'CHSS Lecture Theatre 1', -6.211153, 35.796325, 'VERIFIED', true),
(2, 14, 'Social Classrooms', 'Social Classrooms', 'Social Classrooms', 'SOC-CR', 'Social Classrooms', -6.212289, 35.795028, 'VERIFIED', true),
(2, 14, 'School of Social Science', 'School of Social Science', 'Social Science', 'SSS', 'School of Social Science', -6.211610, 35.796953, 'VERIFIED', true),
(2, 14, 'Social Science Library', 'Social Science Library', 'Social Library', 'SSL', 'Social Science Library', -6.211230, 35.798080, 'VERIFIED', true),
(2, 14, 'Confucius Institute at UDOM', 'Confucius Institute at UDOM', 'Confucius UDOM', 'CI-UDOM', 'Confucius Institute at UDOM', -6.212925, 35.788163, 'VERIFIED', true),
(2, 14, 'College of Business Studies Lecture Theatre 1', 'College of Business Studies Lecture Theatre 1', 'CBS LT1', 'CBS-LT1', 'College of Business Studies Lecture Theatre 1', -6.214218, 35.786755, 'VERIFIED', true),
(2, 14, 'College of Business Studies Lecture Theatre 2', 'College of Business Studies Lecture Theatre 2', 'CBS LT2', 'CBS-LT2', 'College of Business Studies Lecture Theatre 2', -6.213932, 35.787347, 'VERIFIED', true),
(2, 14, 'CBSL Class Theatres (C1)', 'CBSL Class Theatres (C1)', 'CBSL C1', 'CBSL-C1', 'CBSL Class Theatres (C1)', -6.213308, 35.786173, 'VERIFIED', true),
(2, 14, 'CBSL Class Theatres', 'CBSL Class Theatres', 'CBSL Theatres', 'CBSL-CT', 'CBSL Class Theatres', -6.213353, 35.786192, 'VERIFIED', true),
(2, 14, 'CBSL', 'College of Business and Social Law', 'CBSL', 'CBSL', 'College of Business Studies and Law', -6.214611, 35.786269, 'VERIFIED', true),
(2, 14, 'CBSL Studio', 'CBSL Studio', 'CBSL Studio', 'CBSL-ST', 'CBSL Studio', -6.215044, 35.787617, 'VERIFIED', true),
(2, 14, 'UDOM CBSL AB2', 'UDOM CBSL AB2', 'CBSL AB2', 'CBSL-AB2', 'UDOM CBSL AB2', -6.214569, 35.786264, 'VERIFIED', true),
(2, 14, 'College of Business and Law (CBSL)', 'College of Business and Law (CBSL)', 'CBSL Main', 'CBSL-MAIN', 'College of Business and Law', -6.216157, 35.788814, 'VERIFIED', true),
(2, 14, 'Humanities Block 4', 'Humanities Block 4', 'Block 4', 'HUM-B4', 'Humanities Block 4', -6.219793, 35.781618, 'VERIFIED', true),
(2, 14, 'CIVE Laboratory', 'CIVE Laboratory', 'CIVE Lab', 'CIVE-LAB1', 'CIVE Laboratory', -6.214373, 35.809153, 'VERIFIED', true),
(2, 14, 'CIVE Laboratory (2)', 'CIVE Laboratory (2)', 'CIVE Lab 2', 'CIVE-LAB2', 'CIVE Laboratory (2)', -6.214394, 35.809149, 'VERIFIED', true),
(2, 14, 'College of Informatics Lecture Theatre', 'College of Informatics Lecture Theatre', 'CIVE LT', 'CIVE-LT', 'College of Informatics Lecture Theatre', -6.213816, 35.809611, 'VERIFIED', true),
(2, 14, 'Lecture Room Library Toilet', 'Lecture Room Library Toilet', 'CIVE Toilet', 'LT-WC', 'Lecture Room Library Facility', -6.214049, 35.809586, 'VERIFIED', true),
(2, 14, 'Computer LAB 2 Electronics', 'Computer LAB 2 Electronics', 'Comp Lab 2', 'LAB2-E', 'Computer LAB 2 Electronics', -6.215035, 35.809613, 'VERIFIED', true),
(2, 14, 'CIVE Library', 'CIVE Library', 'Informatics Library', 'CIVE-LIB', 'CIVE Library', -6.215117, 35.810820, 'VERIFIED', true),
(2, 14, 'CIVE Academic Block', 'CIVE Academic Block', 'Academic Block', 'CIVE-AB', 'CIVE Academic Block', -6.216063, 35.808468, 'VERIFIED', true),
(2, 14, 'College of Informatics and Virtual Education (CIVE) Admin Block', 'CIVE Admin Block', 'CIVE Admin', 'CIVE-ADM', 'College of Informatics and Virtual Education (CIVE) Admin Block', -6.217047, 35.808876, 'VERIFIED', true),
(2, 14, 'College of Informatics and Virtual Education', 'College of Informatics and Virtual Education (CIVE)', 'CIVE', 'CIVE', 'College of Informatics and Virtual Education', -6.218031, 35.810536, 'VERIFIED', true),
(2, 14, 'College Library (College of Informatics)', 'College Library (College of Informatics)', 'Informatics Library', 'INF-LIB', 'College Library (College of Informatics)', -6.215717, 35.812168, 'VERIFIED', true),
(2, 14, 'CIVE Auditorium', 'CIVE Auditorium', 'CIVE Audi', 'CIVE-AUD', 'CIVE Auditorium', -6.216787, 35.811130, 'VERIFIED', true),
(2, 14, 'College of Earth Sciences and Engineering (COESE)', 'College of Earth Sciences and Engineering', 'COESE', 'COESE', 'College of Earth Sciences and Engineering (COESE)', -6.228895, 35.810513, 'VERIFIED', true),
(2, 14, 'CNMS - UDOM', 'College of Natural and Mathematical Sciences', 'CNMS', 'CNMS', 'College of Natural and Mathematical Sciences (CNMS)', -6.221539, 35.821225, 'VERIFIED', true),
(2, 14, 'COED Lecture Theatre', 'COED Lecture Theatre', 'COED LT', 'COED-LT', 'COED Lecture Theatre', -6.231013, 35.836694, 'VERIFIED', true),
(2, 14, 'COED Lecture Room 1', 'COED Lecture Room 1', 'COED LR1', 'COED-LR1', 'COED Lecture Room 1', -6.231285, 35.839093, 'VERIFIED', true),
(2, 14, 'SOMD Classes Allied', 'SOMD Classes Allied', 'SOMD Classes', 'SOMD-CL', 'School of Medicine Classes Allied', -6.225756, 35.850703, 'VERIFIED', true),
(2, 14, 'CHAS Library', 'College of Health Sciences Library', 'CHAS Library', 'CHAS-LIB', 'CHAS Library', -6.226005, 35.851531, 'VERIFIED', true),
(2, 14, 'CHAS Laboratory', 'CHAS Laboratory', 'CHAS Lab', 'CHAS-LAB', 'CHAS Laboratory', -6.227154, 35.850306, 'VERIFIED', true),
(2, 14, 'CHAS Classes', 'CHAS Classes', 'CHAS Classrooms', 'CHAS-CL', 'CHAS Classes', -6.224825, 35.851929, 'VERIFIED', true),
(2, 14, 'UDOM College of Health Sciences (CHAS)', 'College of Health Sciences', 'CHAS', 'CHAS', 'UDOM College of Health Sciences (CHAS)', -6.225719, 35.850636, 'VERIFIED', true),
(2, 14, 'Benjamin Mkapa Institute of Health and Allied Sciences', 'Benjamin Mkapa Institute of Health and Allied Sciences', 'BMIHAS', 'BMIHAS', 'Benjamin Mkapa Institute of Health and Allied Sciences', -6.230697, 35.847399, 'VERIFIED', true);

-- CATEGORY 15: ADMINISTRATIVE & OFFICIAL BUILDINGS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 15, 'University of Dodoma Central Administration', 'University of Dodoma Central Administration Building', 'Central Admin', 'ADMIN', 'Central Administration Block', -6.204149, 35.797690, 'VERIFIED', true),
(2, 15, 'Ofisi ya Wakili wa Serikali Mkuu', 'Ofisi ya Wakili wa Serikali Mkuu', 'Wakili Mkuu', 'OWSM', 'Ofisi ya Wakili wa Serikali Mkuu', -6.213912, 35.789967, 'VERIFIED', true),
(2, 15, 'Utumishi Offices', 'Utumishi Offices', 'Utumishi', 'UTUMISHI', 'Utumishi Offices', -6.214472, 35.789997, 'VERIFIED', true),
(2, 15, 'e-Government Agency Dodoma', 'e-Government Agency Dodoma', 'eGA Dodoma', 'EGA', 'e-Government Agency Dodoma', -6.214670, 35.790027, 'VERIFIED', true),
(2, 15, 'UDOM Frame Office', 'UDOM Frame Office', 'Frame Office', 'FRAME', 'UDOM Frame Office', -6.218470, 35.782494, 'VERIFIED', true),
(2, 15, 'e-Government RIDC', 'e-Government Regional Innovation & Data Centre', 'eGA RIDC', 'RIDC', 'e-Government RIDC', -6.214730, 35.808328, 'VERIFIED', true),
(2, 15, 'Administration Block College of Education (COED)', 'COED Administration Block', 'COED Admin', 'COED-ADM', 'Administration Block College of Education (COED)', -6.222293, 35.836856, 'VERIFIED', true);

-- CATEGORY 16: STUDENT ACCOMMODATION & HOSTELS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 16, 'JOT Apartments', 'JOT Apartments', 'JOT Hostel', 'JOT', 'JOT Apartments', -6.202557, 35.803443, 'VERIFIED', true),
(2, 16, 'Bonderi Hostel', 'Bonderi Hostel', 'Bonderi', 'BONDERI', 'Bonderi Hostel', -6.220304, 35.781929, 'VERIFIED', true),
(2, 16, 'CIVE Block 2', 'CIVE Block 2 Hostel', 'Block 2', 'CIVE-B2', 'CIVE Hostel Block 2', -6.215922, 35.814490, 'VERIFIED', true),
(2, 16, 'Block 3 - F33 WE THE BEST', 'Block 3 - F33 WE THE BEST', 'Block 3 F33', 'CIVE-B3', 'CIVE Block 3 Hostel', -6.216334, 35.814821, 'VERIFIED', true),
(2, 16, 'Block A (COED)', 'Block A COED Hostel', 'Block A', 'COED-BA', 'COED Block A Hostel', -6.223348, 35.833790, 'VERIFIED', true),
(2, 16, 'Block 1 (COED)', 'Block 1 COED Hostel', 'Block 1', 'COED-B1', 'COED Block 1 Hostel', -6.225986, 35.834507, 'VERIFIED', true),
(2, 16, 'COED Block N', 'COED Block N Hostel', 'Block N', 'COED-BN', 'COED Block N Hostel', -6.228794, 35.832823, 'VERIFIED', true),
(2, 16, 'Block Q COED - UDOM', 'Block Q COED Hostel', 'Block Q', 'COED-BQ', 'Block Q COED - UDOM', -6.231267, 35.832589, 'VERIFIED', true),
(2, 16, 'CHAS Student Hostels', 'CHAS Student Hostels', 'CHAS Hostels', 'CHAS-HST', 'CHAS Student Hostels', -6.227865, 35.853596, 'VERIFIED', true);

-- CATEGORY 17: COMMERCIAL, SHOPPING & FINANCIAL SERVICES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 17, 'CRDB Bank (Social)', 'CRDB Bank Social Branch', 'CRDB Social', 'CRDB-SOC', 'CRDB Bank Social Branch', -6.212607, 35.798121, 'VERIFIED', true),
(2, 17, 'Science Shopping Centre', 'Science Shopping Centre', 'Science Mall', 'SCI-SHOP', 'Science Shopping Centre', -6.216002, 35.797474, 'VERIFIED', true),
(2, 17, 'FSIT Solution (Computers & Electric Store)', 'FSIT Solution', 'FSIT Computers', 'FSIT', 'Computers & Electric Store', -6.217807, 35.797978, 'VERIFIED', true),
(2, 17, 'Kariakoo Social', 'Kariakoo Social Market', 'Kariakoo Social', 'KK-SOC', 'Kariakoo Social Commercial Area', -6.217881, 35.798086, 'VERIFIED', true),
(2, 17, 'UDOM TECH Shopping Mall', 'UDOM TECH Shopping Mall', 'TECH Mall', 'TECH-MALL', 'UDOM TECH Shopping Mall', -6.219578, 35.801155, 'VERIFIED', true),
(2, 17, 'Block 14 UDOM TECH', 'Block 14 UDOM TECH', 'TECH Block 14', 'TECH-B14', 'Block 14 UDOM TECH Commercial Block', -6.219910, 35.800991, 'VERIFIED', true),
(2, 17, 'Charzy Said Investment', 'Charzy Said Investment', 'Charzy Investment', 'CHARZY', 'Charzy Said Investment Store', -6.219878, 35.800199, 'VERIFIED', true),
(2, 17, 'UDOM Bank', 'UDOM Bank Agency', 'UDOM Bank', 'BANK-MAIN', 'UDOM Campus Financial Agency', -6.219010, 35.804878, 'VERIFIED', true),
(2, 17, 'Manguyini Stationery', 'Manguyini Stationery', 'Manguyini', 'MANG-STAT', 'Manguyini Stationery & Printing', -6.214969, 35.803439, 'VERIFIED', true),
(2, 17, 'Geonsight Enterprise', 'Geonsight Enterprise', 'Geonsight', 'GEONSIGHT', 'Geonsight Enterprise Shop', -6.214768, 35.809625, 'VERIFIED', true),
(2, 17, 'Smartbet Clothing', 'Smartbet Clothing', 'Smartbet', 'SMARTBET', 'Smartbet Apparel Store', -6.217573, 35.813614, 'VERIFIED', true),
(2, 17, 'NMB Bank (COESE)', 'NMB Bank COESE Branch', 'NMB COESE', 'NMB-COESE', 'NMB Bank Branch COESE', -6.220858, 35.809869, 'VERIFIED', true),
(2, 17, 'Anga', 'Anga Commercial Centre', 'Anga Shop', 'ANGA', 'Anga Store', -6.220929, 35.810373, 'VERIFIED', true),
(2, 17, 'Kingston Asali', 'Kingston Asali Honey & Mart', 'Kingston Asali', 'KINGSTON', 'Kingston Asali Shop', -6.220643, 35.812295, 'VERIFIED', true),
(2, 17, 'Clothing and Accessories', 'Clothing and Accessories CIVE', 'Clothes Shop', 'CLOTH-CIVE', 'Clothing and Accessories Shop', -6.219211, 35.811876, 'VERIFIED', true),
(2, 17, 'Mangula Car Wash', 'Mangula Car Wash', 'Mangula Carwash', 'MANG-CAR', 'Mangula Car Wash', -6.217412, 35.813156, 'VERIFIED', true),
(2, 17, 'National Housing Rear', 'National Housing Rear', 'National Housing', 'NHC-REAR', 'National Housing Area', -6.210622, 35.824788, 'VERIFIED', true),
(2, 17, 'CRDB Bank (COED)', 'CRDB Bank COED Branch', 'CRDB COED', 'CRDB-COED', 'CRDB Bank Branch COED', -6.224430, 35.836369, 'VERIFIED', true),
(2, 17, 'NMB Bank (COED)', 'NMB Bank COED Branch', 'NMB COED', 'NMB-COED', 'NMB Bank Branch COED', -6.224916, 35.836697, 'VERIFIED', true),
(2, 17, 'Tigo Shop UDOM Branch', 'Tigo Shop UDOM Branch', 'Tigo UDOM', 'TIGO-UDOM', 'Tigo Customer Service Shop', -6.224844, 35.836878, 'VERIFIED', true),
(2, 17, 'Block J Supermarket', 'Block J Supermarket COED', 'Block J Supermarket', 'BLK-J-MART', 'Block J Supermarket', -6.226399, 35.834996, 'VERIFIED', true),
(2, 17, 'Block L (COED)', 'Block L Commercial COED', 'Block L Mart', 'BLK-L-COMM', 'Block L Shops', -6.227518, 35.833279, 'VERIFIED', true),
(2, 17, 'B Studio Graphics, POPOA', 'B Studio Graphics, POPOA', 'B Studio', 'B-STUDIO', 'B Studio Graphics & Printing', -6.228296, 35.832407, 'VERIFIED', true),
(2, 17, 'CRDB ATM', 'CRDB ATM COED', 'CRDB ATM', 'CRDB-ATM', 'CRDB Bank ATM Machine', -6.230312, 35.834322, 'VERIFIED', true),
(2, 17, 'Retiila and Shopping (Clothes)', 'Retiila and Shopping Clothes', 'Retiila Clothes', 'RETIILA', 'Retiila Fashion & Clothes', -6.227695, 35.839311, 'VERIFIED', true),
(2, 17, 'Mjasi GODLI UBA', 'Mjasi GODLI UBA', 'Mjasi UBA', 'MJASI', 'Mjasi GODLI UBA Shop', -6.229988, 35.847610, 'VERIFIED', true),
(2, 17, 'BMH Shopping Centre', 'BMH Shopping Centre', 'BMH Shopping', 'BMH-SHOP', 'BMH Shopping Centre', -6.231715, 35.847572, 'VERIFIED', true),
(2, 17, 'Passy Car Wash', 'Passy Car Wash', 'Passy Carwash', 'PASSY-CW', 'Passy Car Wash', -6.231915, 35.847999, 'VERIFIED', true),
(2, 17, 'CRDB Bank (CHAS/BMH)', 'CRDB Bank CHAS/BMH Branch', 'CRDB CHAS', 'CRDB-BMH', 'CRDB Bank CHAS / BMH Branch', -6.230898, 35.849678, 'VERIFIED', true);

-- CATEGORY 18: DINING & CAFETERIAS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 18, 'Msalato ''B'' Food Point', 'Msalato B Food Point', 'Msalato Food', 'MSALATO-FP', 'Msalato B Food Point', -6.200636, 35.798535, 'VERIFIED', true),
(2, 18, 'Taji', 'Taji Restaurant', 'Taji Food', 'TAJI', 'Taji Dining', -6.212992, 35.798210, 'VERIFIED', true),
(2, 18, 'Cafeta TAYOMI Social', 'Cafeta TAYOMI Social', 'TAYOMI Social', 'TAYOMI-SOC', 'Cafeta TAYOMI Social', -6.216644, 35.797420, 'VERIFIED', true),
(2, 18, 'GR Medics Social', 'GR Medics Social Cafeteria', 'GR Medics', 'GR-MEDICS', 'GR Medics Social Canteen', -6.217961, 35.797890, 'VERIFIED', true),
(2, 18, 'Blessed HO Cafeteria', 'Blessed HO Cafeteria', 'Blessed HO', 'BLESSED-HO', 'Blessed HO Cafeteria', -6.217750, 35.800844, 'VERIFIED', true),
(2, 18, 'John Coffee Shop', 'John Coffee Shop', 'John Coffee', 'JOHN-CAFE', 'John Coffee Shop', -6.219955, 35.800141, 'VERIFIED', true),
(2, 18, 'Break Point Cafeteria', 'Break Point Cafeteria', 'Break Point', 'BREAKPOINT', 'Break Point Cafeteria', -6.215167, 35.799893, 'VERIFIED', true),
(2, 18, 'Alfa Cafeteria', 'Alfa Cafeteria', 'Alfa Cafe', 'ALFA-CAFE', 'Alfa Cafeteria', -6.219467, 35.783027, 'VERIFIED', true),
(2, 18, 'Cafeta Jenga', 'Cafeta Jenga', 'Jenga Cafe', 'JENGA-CAFE', 'Cafeta Jenga', -6.217604, 35.813614, 'VERIFIED', true),
(2, 18, 'COES Cafeteria', 'COES Cafeteria', 'COES Cafe', 'COES-CAFE', 'COES Cafeteria', -6.228409, 35.810418, 'VERIFIED', true),
(2, 18, 'Cafeteria TAYOMI CIVE', 'Cafeteria TAYOMI CIVE', 'TAYOMI CIVE', 'TAYOMI-CIVE', 'Cafeteria TAYOMI CIVE', -6.217409, 35.815566, 'VERIFIED', true),
(2, 18, 'COED Cafeteria', 'COED Cafeteria', 'COED Cafe', 'COED-CAFE', 'COED Cafeteria', -6.224650, 35.836233, 'VERIFIED', true),
(2, 18, 'Mwalimu Restaurant', 'Mwalimu Restaurant', 'Mwalimu Rest', 'MWALIMU-REST', 'Mwalimu Restaurant', -6.230514, 35.847439, 'VERIFIED', true),
(2, 18, 'CHAS Cafeteria', 'CHAS Cafeteria', 'CHAS Cafe', 'CHAS-CAFE', 'CHAS Cafeteria', -6.226807, 35.852978, 'VERIFIED', true);

-- CATEGORY 19: MEDICAL & HEALTH FACILITIES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 19, 'University Hospital', 'University of Dodoma Hospital', 'UDOM Hospital', 'UDOM-HOSP', 'University Hospital', -6.214288, 35.795884, 'VERIFIED', true),
(2, 19, 'CIVE Specialized Hospital', 'CIVE Specialized Hospital', 'CIVE Hospital', 'CIVE-HOSP', 'CIVE Specialized Hospital', -6.217020, 35.813409, 'VERIFIED', true),
(2, 19, 'BMH Trauma and Critical Care Unit', 'BMH Trauma and Critical Care Unit', 'BMH Trauma', 'BMH-TRAUMA', 'BMH Trauma & Critical Care Unit', -6.230229, 35.846953, 'VERIFIED', true),
(2, 19, 'Benjamin Mkapa Hospital (BMH)', 'Benjamin Mkapa Hospital', 'BMH Hospital', 'BMH', 'Benjamin Mkapa Hospital (BMH)', -6.230897, 35.847251, 'VERIFIED', true),
(2, 19, 'BMH Mortuary', 'BMH Mortuary Unit', 'BMH Mortuary', 'BMH-MORT', 'BMH Mortuary', -6.231097, 35.846229, 'VERIFIED', true),
(2, 19, 'BMH Pombe Salama', 'BMH Pombe Salama Unit', 'Pombe Salama', 'BMH-PS', 'BMH Health Centre', -6.231977, 35.847407, 'VERIFIED', true),
(2, 19, 'Dietitian HAZURU', 'Dietitian HAZURU Clinic', 'Dietitian Hazuru', 'HAZURU', 'Dietitian HAZURU Clinic', -6.225756, 35.850703, 'VERIFIED', true);

-- CATEGORY 20: SPORTS, RECREATION & SOCIAL FACILITIES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 20, 'Basket Ground (Social)', 'Social Basketball Ground', 'Social Basketball', 'SOC-BB', 'Social Basketball Ground', -6.215394, 35.797911, 'VERIFIED', true),
(2, 20, 'Block 3 Basketball Ground', 'Block 3 Basketball Ground', 'Block 3 Basketball', 'B3-BB', 'Block 3 Basketball Ground', -6.215686, 35.797673, 'VERIFIED', true),
(2, 20, 'Social - Football Ground', 'Social Football Ground', 'Social Football Pitch', 'SOC-FB', 'Social Football Pitch', -6.216786, 35.804673, 'VERIFIED', true),
(2, 20, 'Social - Volleyball Ground', 'Social Volleyball Ground', 'Social Volleyball', 'SOC-VB', 'Social Volleyball Court', -6.216151, 35.802068, 'VERIFIED', true),
(2, 20, 'COESE Basketball Court', 'COESE Basketball Court', 'COESE Basketball', 'COESE-BB', 'COESE Basketball Court', -6.216427, 35.801760, 'VERIFIED', true),
(2, 20, 'Social - Netball Ground', 'Social Netball Ground', 'Social Netball', 'SOC-NB', 'Social Netball Ground', -6.215840, 35.802392, 'VERIFIED', true),
(2, 20, 'Basketball Ground', 'Central Basketball Ground', 'Campus Basketball', 'CENTRAL-BB', 'Basketball Ground', -6.215424, 35.797913, 'VERIFIED', true),
(2, 20, 'JFS Game Play, Movies & Series', 'JFS Game Play & Cinema', 'JFS Movies', 'JFS-GAME', 'JFS Gaming & Cinema Hub', -6.218165, 35.782394, 'VERIFIED', true),
(2, 20, 'CIVE Volleyball Playground', 'CIVE Volleyball Playground', 'CIVE Volleyball', 'CIVE-VB', 'CIVE Volleyball Court', -6.212101, 35.806735, 'VERIFIED', true),
(2, 20, 'CIVE Football Ground', 'CIVE Football Ground', 'CIVE Football Pitch', 'CIVE-FB', 'CIVE Football Ground', -6.211362, 35.807095, 'VERIFIED', true),
(2, 20, 'CIVE Netball Ground', 'CIVE Netball Ground', 'CIVE Netball', 'CIVE-NB', 'CIVE Netball Court', -6.212008, 35.806392, 'VERIFIED', true),
(2, 20, 'CIVE Basketball Playground', 'CIVE Basketball Playground', 'CIVE Basketball', 'CIVE-BB', 'CIVE Basketball Playground', -6.212371, 35.806607, 'VERIFIED', true),
(2, 20, 'COED Football Ground', 'COED Football Ground', 'COED Football Pitch', 'COED-FB', 'COED Football Ground', -6.227060, 35.839249, 'VERIFIED', true),
(2, 20, 'COED Basketball Ground', 'COED Basketball Ground', 'COED Basketball', 'COED-BB', 'COED Basketball Ground', -6.227204, 35.840360, 'VERIFIED', true),
(2, 20, 'COED Volleyball Playground', 'COED Volleyball Playground', 'COED Volleyball', 'COED-VB', 'COED Volleyball Court', -6.227674, 35.840199, 'VERIFIED', true),
(2, 20, 'COED Court', 'COED Sports Court', 'COED Court', 'COED-COURT', 'COED Sports Court', -6.225839, 35.840423, 'VERIFIED', true),
(2, 20, 'TIBA Playground UDOM', 'TIBA Playground UDOM', 'TIBA Ground', 'TIBA-PLAY', 'TIBA Playground UDOM', -6.220229, 35.893763, 'VERIFIED', true);

-- CATEGORY 21: PLACES OF WORSHIP
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 21, 'CHSS Mosque', 'CHSS Mosque', 'CHSS Msikiti', 'CHSS-MSK', 'CHSS Mosque', -6.217000, 35.798046, 'VERIFIED', true),
(2, 21, 'Masjid Social', 'Masjid Social', 'Msikiti Social', 'SOC-MSK', 'Social Mosque', -6.217186, 35.798016, 'VERIFIED', true),
(2, 21, 'Bonderi UDOM (Place of Worship)', 'Bonderi Place of Worship', 'Bonderi Chapel', 'BONDER-WSR', 'Bonderi Worship Centre', -6.216332, 35.799508, 'VERIFIED', true),
(2, 21, 'Orthodox Church Dodoma - UDOM (CBCL)', 'Orthodox Church Dodoma UDOM', 'Orthodox UDOM', 'ORTH-UDOM', 'Orthodox Church Dodoma', -6.213088, 35.785957, 'VERIFIED', true),
(2, 21, 'Jumba La Ufalme', 'Jumba La Ufalme Worship Centre', 'Kingdom Hall', 'JUMBA-UFAL', 'Jumba La Ufalme', -6.217396, 35.782719, 'VERIFIED', true),
(2, 21, 'UDOM Central SDA Church', 'UDOM Central SDA Church', 'UDOM SDA', 'SDA-UDOM', 'UDOM Central SDA Church', -6.214004, 35.808359, 'VERIFIED', true),
(2, 21, 'TUCASA CIVE', 'TUCASA CIVE Church', 'TUCASA CIVE', 'TUCASA-CIVE', 'TUCASA CIVE Fellowship', -6.217451, 35.813225, 'VERIFIED', true),
(2, 21, 'USCF - CCT CIVE', 'USCF CCT CIVE Chapel', 'USCF CIVE', 'USCF-CIVE', 'USCF CCT CIVE Chapel', -6.215922, 35.814490, 'VERIFIED', true),
(2, 21, 'Masjid CIVE', 'Masjid CIVE', 'Msikiti CIVE', 'CIVE-MSK', 'Masjid CIVE', -6.215825, 35.813870, 'VERIFIED', true),
(2, 21, 'College of Informatics Mosque (Official)', 'College of Informatics Mosque Official', 'CIVE Main Mosque', 'CIVE-OFF-MSK', 'College of Informatics Official Mosque', -6.216182, 35.814754, 'VERIFIED', true),
(2, 21, 'RC CIVE (Church)', 'Roman Catholic CIVE Church', 'RC CIVE', 'RC-CIVE', 'Roman Catholic CIVE Church', -6.216417, 35.815642, 'VERIFIED', true),
(2, 21, 'COED Mosque', 'COED Mosque', 'Msikiti COED', 'COED-MSK', 'COED Mosque', -6.230787, 35.839108, 'VERIFIED', true),
(2, 21, 'TAG GCC / Yumbu UDOM', 'TAG GCC Yumbu UDOM Church', 'TAG Yumbu', 'TAG-UDOM', 'TAG GCC Church Yumbu', -6.210116, 35.846739, 'VERIFIED', true),
(2, 21, 'MLR1 (Church)', 'MLR1 Worship Centre', 'MLR1 Church', 'MLR1', 'MLR1 Church', -6.224915, 35.851704, 'VERIFIED', true);

-- CATEGORY 22: INFRASTRUCTURE, LANDMARKS & TRANSPORT
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 22, 'Dodoma Viewpoint', 'Dodoma Viewpoint', 'Viewpoint', 'VIEWPOINT', 'Dodoma Panoramic Viewpoint', -6.204215, 35.794860, 'VERIFIED', true),
(2, 22, 'RABRI', 'RABRI Landmark', 'RABRI', 'RABRI', 'RABRI Landmark', -6.200873, 35.794601, 'VERIFIED', true),
(2, 22, 'Chimwaga Complex', 'Chimwaga Complex Building', 'Chimwaga', 'CHIMWAGA', 'Chimwaga Conference & Event Complex', -6.205983, 35.795062, 'VERIFIED', true),
(2, 22, 'Ecowater CHSS 1 Store', 'Ecowater CHSS 1 Store', 'Ecowater CHSS', 'ECO-CHSS', 'Ecowater Station CHSS', -6.212375, 35.796862, 'VERIFIED', true),
(2, 22, 'Ecowater CNMS', 'Ecowater CNMS Store', 'Ecowater CNMS', 'ECO-CNMS', 'Ecowater Station CNMS', -6.218575, 35.800920, 'VERIFIED', true),
(2, 22, 'News', 'UDOM News Station', 'News Station', 'NEWS', 'UDOM News Station', -6.218601, 35.801859, 'VERIFIED', true),
(2, 22, 'CIVE Meeting Hall', 'CIVE Meeting Hall', 'CIVE Hall', 'CIVE-HALL', 'CIVE Meeting Hall', -6.218314, 35.802166, 'VERIFIED', true),
(2, 22, 'CNMS, COESE Shortcut', 'CNMS COESE Pedestrian Shortcut', 'Shortcut', 'SHORTCUT-1', 'CNMS & COESE Connecting Path', -6.217773, 35.804422, 'VERIFIED', true),
(2, 22, 'Ecowater CBE 1 Store', 'Ecowater CBE 1 Store', 'Ecowater CBE', 'ECO-CBE', 'Ecowater Station CBE', -6.218953, 35.782233, 'VERIFIED', true),
(2, 22, 'CIVE Bus Stop', 'CIVE Bus Stop', 'CIVE Bus Stand', 'CIVE-BUS', 'CIVE Bus Stop', -6.217057, 35.807222, 'VERIFIED', true),
(2, 22, 'Smart CCF 400 Trees Project', 'Smart CCF 400 Trees Green Project', 'Smart CCF Trees', 'SMART-TREES', 'Smart CCF 400 Trees Project Area', -6.219531, 35.808010, 'VERIFIED', true),
(2, 22, 'Njia Panda', 'Njia Panda Junction', 'Njia Panda', 'NJIA-PANDA', 'Njia Panda Junction', -6.219211, 35.811876, 'VERIFIED', true),
(2, 22, 'Mlima Jedha COED', 'Mlima Jedha COED Landmark', 'Mlima Jedha', 'JEDHA', 'Mlima Jedha Hill', -6.220310, 35.826111, 'VERIFIED', true),
(2, 22, 'CIVE Water Tank', 'CIVE Water Tank', 'Water Tank', 'CIVE-TANK', 'CIVE Water Tank Infrastructure', -6.213948, 35.815443, 'VERIFIED', true),
(2, 22, 'Ecowater CIVE Store', 'Ecowater CIVE Store', 'Ecowater CIVE', 'ECO-CIVE', 'Ecowater Station CIVE', -6.216123, 35.813014, 'VERIFIED', true),
(2, 22, 'Tanzania Programes', 'Tanzania Programes Landmark', 'Tanzania Programes', 'TZ-PROG', 'Tanzania Programes Centre', -6.217412, 35.813156, 'VERIFIED', true),
(2, 22, 'Patamrisi - UDOM', 'Patamrisi - UDOM Landmark', 'Patamrisi', 'PATAMRISI', 'Patamrisi Area', -6.216356, 35.815660, 'VERIFIED', true),
(2, 22, 'Mwisho wa Lami', 'Mwisho wa Lami Bus Terminus', 'Mwisho wa Lami', 'MWISHO-LAMI', 'Mwisho wa Lami Road End & Bus Stop', -6.237480, 35.820603, 'VERIFIED', true),
(2, 22, 'Udom-Iyumbu Hills', 'Udom Iyumbu Hills Landmark', 'Iyumbu Hills', 'IYUMBU-HILLS', 'Iyumbu Hills View', -6.226149, 35.825557, 'VERIFIED', true),
(2, 22, 'Njia Panda Utawala Mkuu (UDOM)', 'Njia Panda Utawala Mkuu UDOM', 'Njia Panda Admin', 'NJIA-ADMIN', 'Central Admin Road Junction', -6.220474, 35.824229, 'VERIFIED', true),
(2, 22, 'Elmika Mtandaoni', 'Elmika Mtandaoni Centre', 'Elmika', 'ELMIKA', 'Elmika Mtandaoni Centre', -6.211455, 35.824014, 'VERIFIED', true),
(2, 22, 'Dar es Salaam', 'Dar es Salaam Bus Stop Point', 'Dar Stop', 'DAR-POINT', 'Dar es Salaam Bus Point', -6.225827, 35.834977, 'VERIFIED', true),
(2, 22, 'Dr. Rich', 'Dr. Rich Point', 'Dr Rich', 'DR-RICH', 'Dr. Rich Location', -6.232323, 35.833323, 'VERIFIED', true),
(2, 22, 'Block T Bus Stop', 'Block T Bus Stop COED', 'Block T Bus', 'BUS-BLKT', 'Block T Bus Stop', -6.234020, 35.832553, 'VERIFIED', true),
(2, 22, 'UDOM Waste Water Sewage', 'UDOM Waste Water Sewage Plant', 'Sewage Plant', 'SEWAGE', 'UDOM Waste Water Plant', -6.222716, 35.845107, 'VERIFIED', true),
(2, 22, 'Tiba Staff Kitabu', 'Tiba Staff Kitabu Office', 'Tiba Staff', 'TIBA-STAFF', 'Tiba Staff Kitabu', -6.228664, 35.848497, 'VERIFIED', true),
(2, 22, 'Ecowater CHS 1 Store', 'Ecowater CHS 1 Store', 'Ecowater CHS', 'ECO-CHS', 'Ecowater Station CHS', -6.227746, 35.853086, 'VERIFIED', true);

-- CATEGORY 23: PARKING AREAS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 23, 'Block 15 Parking (CBSL)', 'Block 15 Parking CBSL', 'CBSL Parking', 'PARK-CBSL15', 'Block 15 Parking Area', -6.217424, 35.781843, 'VERIFIED', true),
(2, 23, 'College of Information Parking Area', 'College of Information Parking Area', 'CIVE Parking', 'PARK-CIVE', 'College of Information Parking Area', -6.215502, 35.809644, 'VERIFIED', true),
(2, 23, 'Bus Parking', 'Bus Parking Area UDOM', 'Bus Parking', 'PARK-BUS', 'Bus Parking Area', -6.230663, 35.848671, 'VERIFIED', true),
(2, 23, 'CHAS Library Parking', 'CHAS Library Parking Area', 'CHAS Parking', 'PARK-CHAS', 'CHAS Library Parking Area', -6.226085, 35.851892, 'VERIFIED', true),
(2, 23, 'CAFE Parking', 'CAFE Parking Area', 'CAFE Parking', 'PARK-CAFE', 'CAFE Parking Area', -6.226247, 35.853375, 'VERIFIED', true);

-- CATEGORY 24: SCHOOLS & PRIMARY EDUCATION
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 24, 'UDOM - Sample School', 'UDOM Sample Primary School', 'UDOM Sample School', 'SCH-SAMPLE', 'UDOM Sample Primary School', -6.213098, 35.849226, 'VERIFIED', true),
(2, 24, 'Kanaani Primary School', 'Kanaani Primary School', 'Kanaani Primary', 'SCH-KANAANI', 'Kanaani Primary School', -6.230878, 35.859853, 'VERIFIED', true);

-- SYNC SEQUENCES
ALTER TABLE university ALTER COLUMN id RESTART WITH 10;
ALTER TABLE campus ALTER COLUMN id RESTART WITH 10;
ALTER TABLE category ALTER COLUMN id RESTART WITH 50;
