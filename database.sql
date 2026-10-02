-- CAMPNAV CANONICAL DATABASE DUMP
-- Combined Schema and Data for GitHub
-- Generated on 2026-09-28

-- Drop existing tables to ensure a clean state
DROP TABLE IF EXISTS comment_attachment CASCADE;
DROP TABLE IF EXISTS issue_comment CASCADE;
DROP TABLE IF EXISTS issue_evidence CASCADE;
DROP TABLE IF EXISTS issue_timeline CASCADE;
DROP TABLE IF EXISTS issue_report CASCADE;
DROP TABLE IF EXISTS community_user CASCADE;
DROP TABLE IF EXISTS department CASCADE;
DROP TABLE IF EXISTS playlist_channels CASCADE;
DROP TABLE IF EXISTS channel CASCADE;
DROP TABLE IF EXISTS playlist CASCADE;
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

-- 5. DEPARTMENT SCHEMA
CREATE TABLE department (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    head_officer_id VARCHAR(50),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. COMMUNITY USER SCHEMA
CREATE TABLE community_user (
    id SERIAL PRIMARY KEY,
    external_id VARCHAR(100) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(50),
    role VARCHAR(50) DEFAULT 'CITIZEN',
    department_id INTEGER REFERENCES department(id),
    reputation_score INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- 7. ISSUE REPORT SCHEMA
CREATE TABLE issue_report (
    id SERIAL PRIMARY KEY,
    external_id VARCHAR(50) UNIQUE NOT NULL,
    category VARCHAR(50) NOT NULL,
    sub_category VARCHAR(100),
    description TEXT NOT NULL,
    voice_transcription TEXT,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    address TEXT,
    street VARCHAR(255),
    ward VARCHAR(100),
    district VARCHAR(100),
    region VARCHAR(100),
    nearby_landmark VARCHAR(255),
    reporter_latitude DOUBLE PRECISION,
    reporter_longitude DOUBLE PRECISION,
    distance_to_issue DOUBLE PRECISION,
    location_accuracy FLOAT,
    video_url TEXT,
    voice_url TEXT,
    before_image_url TEXT,
    after_image_url TEXT,
    status VARCHAR(50) DEFAULT 'SUBMITTED',
    user_severity VARCHAR(20),
    calculated_priority VARCHAR(20),
    impact_score INTEGER DEFAULT 0,
    is_emergency BOOLEAN DEFAULT FALSE,
    reporter_id INTEGER REFERENCES community_user(id),
    assigned_department_id INTEGER REFERENCES department(id),
    assigned_officer_id INTEGER REFERENCES community_user(id),
    supports_count INTEGER DEFAULT 0,
    verification_count INTEGER DEFAULT 0,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Supporting table for issue evidence
CREATE TABLE issue_evidence (
    issue_id INTEGER NOT NULL REFERENCES issue_report(id) ON DELETE CASCADE,
    evidence_url TEXT NOT NULL
);

-- 8. ISSUE COMMENT SCHEMA
CREATE TABLE issue_comment (
    id SERIAL PRIMARY KEY,
    issue_id INTEGER REFERENCES issue_report(id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES community_user(id),
    comment_text TEXT NOT NULL,
    is_official BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE comment_attachment (
    comment_id INTEGER NOT NULL REFERENCES issue_comment(id) ON DELETE CASCADE,
    attachment_url TEXT NOT NULL
);

-- 9. ISSUE TIMELINE SCHEMA
CREATE TABLE issue_timeline (
    id SERIAL PRIMARY KEY,
    issue_id INTEGER REFERENCES issue_report(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    message TEXT,
    actor_id INTEGER REFERENCES community_user(id),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. IPTV ENTERTAINMENT SCHEMAS
CREATE TABLE channel (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    url VARCHAR(255),
    logo VARCHAR(255),
    category VARCHAR(100)
);

CREATE TABLE playlist (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    url VARCHAR(255)
);

CREATE TABLE playlist_channels (
    playlist_id INTEGER REFERENCES playlist(id),
    channels_id INTEGER REFERENCES channel(id)
);

-- SEED DATA - Universities
INSERT INTO university (id, external_id, name, official_name, short_name, city, country, latitude, longitude, default_zoom, description, logo_url, sort_order, campus_area_hectares) VALUES
(1, 'UDSM', 'University of Dar es Salaam', 'University of Dar es Salaam', 'UDSM', 'Dar es Salaam', 'Tanzania', -6.7801, 39.2041, 15.0, 'Hekima ni Uhuru', 'https://upload.wikimedia.org/wikipedia/en/2/2a/University_of_Dar_es_Salaam_Logo.png', 1, 657.0),
(2, 'UDOM', 'University of Dodoma', 'University of Dodoma', 'UDOM', 'Dodoma', 'Tanzania', -6.2033, 35.8000, 14.0, 'Embracing Knowledge', 'https://upload.wikimedia.org/wikipedia/en/1/1b/UDOM_Logo.png', 2, 6000.0),
(3, 'MUST', 'Mbeya University of Science and Technology', 'Mbeya University of Science and Technology', 'MUST', 'Mbeya', 'Tanzania', -8.94315, 33.41636, 15.0, 'Science and Technology for Development', 'https://upload.wikimedia.org/wikipedia/en/a/a2/Mbeya_University_of_Science_and_Technology_Logo.png', 3, NULL),
(4, 'SUA', 'Sokoine University of Agriculture', 'Sokoine University of Agriculture', 'SUA', 'Morogoro', 'Tanzania', -6.8475, 37.6591, 15.0, 'Ardhi ni Hazina', 'https://upload.wikimedia.org/wikipedia/en/3/3d/Sua_logo.png', 4, NULL);

-- Seed Campus
INSERT INTO campus (id, external_id, university_id, name) VALUES
(1, 'UDSM-MLIMANI', 1, 'Mwalimu Julius K. Nyerere Mlimani Campus'),
(2, 'UDOM-MAIN', 2, 'University of Dodoma Main Campus'),
(3, 'MUST-MAIN', 3, 'Mbeya University Main Campus'),
(4, 'SUA-MAIN', 4, 'Sokoine University of Agriculture Main Campus');

-- ============================================================================
-- BEGIN UDSM SPECIFIC DATA (From Flyway V8)
-- ============================================================================
DELETE FROM campus_location WHERE university_id = 1;
DELETE FROM category WHERE university_id = 1;

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
(1, 1, 'Block Q', 'Block Q', 'Block Q', 'Block Q', 'Block Q', -6.783515, 39.206983, 'VERIFIED', true),
(1, 1, 'CoICT', 'College of Information and Communication Technologies', 'CoICT; Sayansi Campus; Kijitonyama Campus; ICT College', 'CoICT', 'College of Information and Communication Technologies at Kijitonyama Sayansi', -6.771471, 39.239928, 'VERIFIED', true),
(1, 1, 'SJMC', 'School of Journalism and Mass Communication', 'SJMC; Journalism School; Mikocheni Campus', 'SJMC', 'School of Journalism and Mass Communication at Mikocheni', -6.770842, 39.250056, 'VERIFIED', true);

-- CATEGORY 2: LECTURE HALLS & CLASSROOMS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(1, 2, 'Nkrumah Hall', 'Nkrumah Hall', 'Nkrumah', 'Nkrumah', 'Nkrumah Hall', -6.780925, 39.204594, 'VERIFIED', true),
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
(1, 3, 'Dr. E. Ndiralema (Water Quality)', 'Dr. E. Ndiralema (Water Quality)', 'Water Quality', 'Water', 'Dr. E. Ndiralema (Water Quality)', -6.781671, 39.207123, 'VERIFIED', true);

-- ============================================================================
-- BEGIN UDOM SPECIFIC DATA (From Flyway V12)
-- ============================================================================
DELETE FROM campus_location WHERE university_id = 2;
DELETE FROM category WHERE university_id = 2;

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

-- CATEGORY 14: ACADEMIC FACILITIES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 14, 'College of Humanities and Social Sciences (CHSS)', 'College of Humanities and Social Sciences', 'CHSS', 'UDOM-CHSS', 'College of Humanities and Social Sciences', -6.208465, 35.845942, 'VERIFIED', true),
(2, 14, 'College of Education (COED)', 'College of Education', 'COED', 'UDOM-COED', 'College of Education at UDOM', -6.216347, 35.850400, 'VERIFIED', true),
(2, 14, 'College of Informatics and Virtual Education (CIVE)', 'College of Informatics and Virtual Education', 'CIVE', 'UDOM-CIVE', 'College of Informatics and Virtual Education', -6.219803, 35.834000, 'VERIFIED', true),
(2, 14, 'College of Natural and Mathematical Sciences (CNMS)', 'College of Natural and Mathematical Sciences', 'CNMS', 'UDOM-CNMS', 'College of Natural and Mathematical Sciences', -6.223400, 35.838900, 'VERIFIED', true),
(2, 14, 'College of Earth Sciences and Engineering (CoESE)', 'College of Earth Sciences and Engineering', 'CoESE', 'UDOM-CoESE', 'College of Earth Sciences and Engineering', -6.225100, 35.841200, 'VERIFIED', true),
(2, 14, 'College of Health Sciences (CHS)', 'College of Health Sciences', 'CHS', 'UDOM-CHS', 'College of Health Sciences', -6.230500, 35.855000, 'VERIFIED', true),
(2, 14, 'UDOM Library', 'University of Dodoma Main Library', 'UDOM Library', 'UDOM-LIB-MAIN', 'Main University Library', -6.209123, 35.846500, 'VERIFIED', true),
(2, 14, 'CHSS Library', 'CHSS College Library', 'CHSS Library', 'UDOM-LIB-CHSS', 'College of Humanities and Social Sciences Library', -6.208100, 35.845200, 'VERIFIED', true),
(2, 14, 'CIVE Library', 'CIVE College Library', 'CIVE Library', 'UDOM-LIB-CIVE', 'College of Informatics Library', -6.219500, 35.833800, 'VERIFIED', true),
(2, 14, 'CIVE LR1', 'CIVE Lecture Room 1', 'CIVE LR1', 'UDOM-LR1', 'CIVE Lecture Room 1', -6.219200, 35.833500, 'VERIFIED', true),
(2, 14, 'CHSS Auditorium', 'CHSS Main Auditorium', 'CHSS Auditorium', 'UDOM-AUD-CHSS', 'CHSS Main Auditorium', -6.208800, 35.846100, 'VERIFIED', true);

-- CATEGORY 15: ADMINISTRATIVE & OFFICIAL BUILDINGS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 15, 'UDOM Central Administration', 'UDOM Vice Chancellor & Central Administration Building', 'UDOM Admin', 'UDOM-HQ', 'Central Administration Building', -6.207800, 35.844500, 'VERIFIED', true),
(2, 15, 'COED Administration Block', 'COED Principal & Faculty Administration Block', 'COED Admin', 'UDOM-ADM-COED', 'COED Faculty Administration Block', -6.215800, 35.849800, 'VERIFIED', true),
(2, 15, 'CIVE Administration Block', 'CIVE Principal & Faculty Administration Block', 'CIVE Admin', 'UDOM-ADM-CIVE', 'CIVE Faculty Administration Block', -6.219100, 35.834200, 'VERIFIED', true);

-- CATEGORY 16: STUDENT ACCOMMODATION & HOSTELS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 16, 'CHSS Hostel Block 1', 'CHSS Student Hostel Block 1', 'CHSS Hostel 1', 'UDOM-HST-CHSS1', 'Student Residential Hostel Block 1', -6.206500, 35.847200, 'VERIFIED', true),
(2, 16, 'CHSS Hostel Block 2', 'CHSS Student Hostel Block 2', 'CHSS Hostel 2', 'UDOM-HST-CHSS2', 'Student Residential Hostel Block 2', -6.206800, 35.847800, 'VERIFIED', true),
(2, 16, 'COED Hostel Block A', 'COED Student Hostel Block A', 'COED Hostel A', 'UDOM-HST-COEDA', 'COED Residential Hostel Block A', -6.214500, 35.851200, 'VERIFIED', true),
(2, 16, 'CIVE Hostel Block 1', 'CIVE Student Hostel Block 1', 'CIVE Hostel 1', 'UDOM-HST-CIVE1', 'CIVE Residential Hostel Block 1', -6.218200, 35.835100, 'VERIFIED', true);

-- CATEGORY 17: COMMERCIAL, SHOPPING & FINANCIAL SERVICES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 17, 'CRDB Bank UDOM Branch', 'CRDB Bank UDOM Branch', 'CRDB UDOM', 'UDOM-BANK-CRDB', 'CRDB Bank Branch & ATM', -6.208900, 35.844100, 'VERIFIED', true),
(2, 17, 'NMB Bank UDOM Branch', 'NMB Bank UDOM Branch', 'NMB UDOM', 'UDOM-BANK-NMB', 'NMB Bank Branch & ATM', -6.208700, 35.843900, 'VERIFIED', true),
(2, 17, 'UDOM Shopping Center', 'UDOM Commercial Shopping Center', 'UDOM Market', 'UDOM-SHOP', 'Campus Shopping Mall & Canteen Area', -6.209500, 35.845500, 'VERIFIED', true);

-- CATEGORY 18: DINING & CAFETERIAS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 18, 'CHSS Main Cafeteria', 'CHSS Main Student Cafeteria', 'CHSS Canteen', 'UDOM-CAF-CHSS', 'CHSS Student & Staff Cafeteria', -6.207900, 35.846800, 'VERIFIED', true),
(2, 18, 'CIVE Cafeteria', 'CIVE Student Cafeteria', 'CIVE Canteen', 'UDOM-CAF-CIVE', 'CIVE Canteen', -6.218800, 35.834600, 'VERIFIED', true);

-- CATEGORY 19: MEDICAL & HEALTH FACILITIES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 19, 'UDOM Hospital', 'UDOM University Health Center Hospital', 'UDOM Hospital', 'UDOM-HOSP', 'Main Campus Hospital & Health Center', -6.229100, 35.854200, 'VERIFIED', true);

-- CATEGORY 20: SPORTS, RECREATION & SOCIAL FACILITIES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 20, 'UDOM Main Stadium', 'UDOM Sports Stadium & Pitch', 'UDOM Stadium', 'UDOM-STAD', 'Main Sports Stadium and Football Field', -6.212000, 35.842000, 'VERIFIED', true),
(2, 20, 'CHSS Basketball Court', 'CHSS Basketball & Volleyball Grounds', 'CHSS Basketball', 'UDOM-BB-CHSS', 'CHSS Basketball Court', -6.207200, 35.848100, 'VERIFIED', true);

-- CATEGORY 21: PLACES OF WORSHIP
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 21, 'UDOM Main Mosque', 'UDOM Central Campus Mosque', 'UDOM Mosque', 'UDOM-MOSQUE', 'Main Campus Mosque', -6.208100, 35.843200, 'VERIFIED', true),
(2, 21, 'UDOM Catholic Chapel', 'UDOM St. Thomas Aquinas Catholic Chapel', 'UDOM RC Chapel', 'UDOM-CHAPEL-RC', 'Roman Catholic Campus Chapel', -6.209800, 35.848200, 'VERIFIED', true);

-- CATEGORY 22: INFRASTRUCTURE, LANDMARKS & TRANSPORT
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 22, 'UDOM Main Gate', 'UDOM Central Main Gate Entrance', 'UDOM Gate', 'UDOM-GATE-MAIN', 'Primary Entrance Gate to UDOM', -6.202500, 35.841000, 'VERIFIED', true);

-- CATEGORY 23: PARKING AREAS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 23, 'CHSS Administration Parking', 'CHSS Main Parking Lot', 'CHSS Parking', 'UDOM-PARK-CHSS', 'CHSS Administration Car Parking Area', -6.207600, 35.844800, 'VERIFIED', true);

-- CATEGORY 24: SCHOOLS & PRIMARY EDUCATION
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(2, 24, 'UDOM Sample School', 'UDOM Sample Primary School', 'UDOM Sample School', 'SCH-SAMPLE', 'UDOM Sample Primary School', -6.213098, 35.849226, 'VERIFIED', true),
(2, 24, 'Kanaani Primary School', 'Kanaani Primary School', 'Kanaani Primary', 'SCH-KANAANI', 'Kanaani Primary School', -6.230878, 35.859853, 'VERIFIED', true);

-- ============================================================================
-- BEGIN MUST SPECIFIC DATA (From Flyway V14 / V16 - Verified Handwritten Logs)
-- ============================================================================
DELETE FROM campus_location WHERE university_id = 3;
DELETE FROM category WHERE university_id = 3;

INSERT INTO category (id, university_id, name, slug, icon_name, description, is_active, sort_order) VALUES
(25, 3, 'Academic & Administrative', 'must-academic-admin', 'school', 'Academic faculties, lecture halls, laboratories, workshops, libraries, and administrative offices', true, 1),
(26, 3, 'Student Accommodation & Housing Blocks', 'must-hostel', 'hotel', 'Student hostels, residential halls, and housing blocks', true, 2),
(27, 3, 'Health & Campus Services', 'must-health-services', 'medical_services', 'Dispensaries, health centers, financial services, and campus stores', true, 3),
(28, 3, 'Commercial, Dining & Shops', 'must-commercial-dining', 'shopping_cart', 'Cafes, restaurants, pubs, shops, internet services, and commercial establishments', true, 4),
(29, 3, 'Sports & Recreation', 'must-sports', 'sports_soccer', 'Football grounds, playgrounds, and recreational sports facilities', true, 5),
(30, 3, 'Places of Worship', 'must-religious', 'account_balance', 'Churches, mosques, and places of worship in and around MUST', true, 6),
(31, 3, 'Surrounding Infrastructure, Streets & Local Governance', 'must-infrastructure', 'place', 'Main gates, streets, local governance offices, and surrounding landmarks', true, 7);

-- CATEGORY 25: ACADEMIC & ADMINISTRATIVE
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(3, 25, 'Administration Block', 'Administration Block', 'Admin Block, Central Admin', 'MUST-ADM-BLK', 'Main Administration Block at MUST', -8.941864, 33.416093, 'VERIFIED', true),
(3, 25, 'MUST Laboratories', 'MUST Laboratories', 'Labs, Science Labs', 'MUST-LABS', 'Mbeya University Science & Technical Laboratories', -8.942646, 33.420825, 'VERIFIED', true),
(3, 25, 'Science Laboratories Point-Mbeya', 'Science Laboratories Point-Mbeya', 'Science Labs Point', 'MUST-SCI-LAB', 'Science Laboratories Point at MUST', -8.941210, 33.417424, 'VERIFIED', true),
(3, 25, 'MUST Workshop', 'MUST Workshop', 'Workshops, Tech Workshop', 'MUST-WORKSHOP', 'Engineering and Technical Workshop at MUST', -8.943113, 33.417101, 'VERIFIED', true),
(3, 25, 'NEW MUST LIBRARY', 'NEW MUST LIBRARY', 'New Library, MUST Library', 'MUST-NEW-LIB', 'New Main Library at MUST', -8.943247, 33.419088, 'VERIFIED', true),
(3, 25, 'New Library Ground', 'New Library Ground', 'Library Ground', 'MUST-LIB-GND', 'Ground area adjacent to the New MUST Library', -8.943004, 33.419660, 'VERIFIED', true),
(3, 25, 'COICT', 'College of Information and Communication Technology (COICT)', 'COICT, CoICT Block', 'MUST-COICT', 'College of Information and Communication Technology', -8.942616, 33.413364, 'VERIFIED', true);

-- CATEGORY 26: STUDENT ACCOMMODATION & HOUSING BLOCKS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(3, 26, 'Mbeya University Hostel', 'Mbeya University Hostel', 'Main Hostel, MUST Hostel', 'MUST-HOSTEL-MAIN', 'Main Student Hostel at Mbeya University', -8.940994, 33.414090, 'VERIFIED', true),
(3, 26, 'NEW MUST HOSTEL', 'NEW MUST HOSTEL', 'New Hostel', 'MUST-NEW-HOSTEL', 'New Student Hostel Complex at MUST', -8.942996, 33.412192, 'VERIFIED', true),
(3, 26, 'Block 6A', 'Block 6A Hostel', 'Hostel 6A, Block 6A', 'MUST-BLK-6A', 'Student Accommodation Block 6A', -8.941006, 33.417407, 'VERIFIED', true),
(3, 26, 'Block 6B', 'Block 6B Hostel', 'Hostel 6B, Block 6B', 'MUST-BLK-6B', 'Student Accommodation Block 6B', -8.941596, 33.417834, 'VERIFIED', true);

-- CATEGORY 27: HEALTH & CAMPUS SERVICES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(3, 27, 'MUST Dispensary', 'MUST Dispensary', 'Dispensary, Health Unit', 'MUST-DISP', 'Campus Health Dispensary and Medical Unit', -8.942367, 33.414672, 'VERIFIED', true),
(3, 27, 'Mbeya Health', 'Mbeya Health Service', 'Mbeya Health', 'MUST-HEALTH', 'Health and Medical Service Facility', -8.940893, 33.417157, 'VERIFIED', true),
(3, 27, 'NMB ATM', 'NMB Bank ATM', 'NMB ATM, NMB', 'MUST-NMB-ATM', 'NMB Automated Teller Machine at MUST', -8.939757, 33.415992, 'VERIFIED', true),
(3, 27, 'MUSTSO Store', 'MUSTSO Store', 'MUSTSO Store, Student Store', 'MUST-MUSTSO-STORE', 'MUST Student Organization Store', -8.941579, 33.416301, 'VERIFIED', true);

-- CATEGORY 28: COMMERCIAL, DINING & SHOPS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(3, 28, 'NYUMBALINK', 'NYUMBALINK Commercial Point', 'NYUMBALINK', 'MUST-NYUMBALINK', 'NYUMBALINK Commercial and Service Shop', -8.941704, 33.416111, 'VERIFIED', true),
(3, 28, 'PRIMENET', 'PRIMENET Internet & Stationery', 'PRIMENET', 'MUST-PRIMENET', 'PRIMENET Internet, Printing & Stationery Services', -8.941821, 33.416288, 'VERIFIED', true),
(3, 28, 'MILK TECHNOLOGY', 'MILK TECHNOLOGY', 'Milk Tech, Dairy Outlet', 'MUST-MILK-TECH', 'Milk Technology Unit / Dairy Outlet', -8.941681, 33.415549, 'VERIFIED', true),
(3, 28, 'Ecowater', 'Ecowater Point', 'Ecowater', 'MUST-ECOWATER', 'Ecowater Drinking Water Station', -8.941579, 33.416301, 'VERIFIED', true),
(3, 28, 'MUST Pub', 'MUST Social Pub', 'MUST Pub, Campus Pub', 'MUST-PUB', 'MUST Campus Pub and Social Hub', -8.940059, 33.417619, 'VERIFIED', true),
(3, 28, 'MUST Shoe Shine', 'MUST Shoe Shine Service', 'Shoe Shine', 'MUST-SHOESHINE', 'Shoe shine and repair service station', -8.939833, 33.417531, 'VERIFIED', true),
(3, 28, 'Deus Comfort Mbeya', 'Deus Comfort Mbeya', 'Deus Comfort', 'MUST-DEUS-COMFORT', 'Deus Comfort Commercial Outlet / Services', -8.943357, 33.419506, 'VERIFIED', true),
(3, 28, 'Maslamba MUST', 'Maslamba MUST', 'Maslamba', 'MUST-MASLAMBA', 'Maslamba MUST Local Establishment', -8.942393, 33.428254, 'VERIFIED', true);

-- CATEGORY 29: SPORTS & RECREATION
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(3, 29, 'MUST Football Ground', 'MUST Football Ground', 'Football Ground, Pitch', 'MUST-PITCH-FB', 'Main Football Pitch and Sports Ground', -8.941125, 33.418738, 'VERIFIED', true),
(3, 29, 'MUST Playground', 'MUST Playground', 'Playground', 'MUST-PLAYGROUND', 'Student Playground and Outdoor Recreation Area', -8.943121, 33.416040, 'VERIFIED', true);

-- CATEGORY 30: PLACES OF WORSHIP
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(3, 30, 'T.A.G SWAYA (Place of Worship)', 'T.A.G Swaya Church', 'TAG Swaya, Swaya Church', 'MUST-TAG-SWAYA', 'Tanzania Assemblies of God (TAG) Swaya Place of Worship', -8.942089, 33.440270, 'VERIFIED', true),
(3, 30, 'Lupeta Catholic Church', 'Lupeta Catholic Church', 'Lupeta RC', 'MUST-LUPETA-RC', 'Lupeta Catholic Church', -8.956882, 33.422782, 'VERIFIED', true),
(3, 30, 'Sisitila Catholic Church', 'Sisitila Catholic Church', 'Sisitila RC', 'MUST-SISITILA-RC', 'Sisitila Catholic Church', -8.953887, 33.402458, 'VERIFIED', true),
(3, 30, 'Maranatha SDA Church', 'Maranatha Seventh-Day Adventist Church', 'Maranatha SDA', 'MUST-SDA-MARANATHA', 'Maranatha SDA Church', -8.938095, 33.426762, 'VERIFIED', true),
(3, 30, 'T.A.G-ICC- Mbeya', 'T.A.G International Christian Center (ICC) Mbeya', 'TAG ICC', 'MUST-TAG-ICC', 'T.A.G ICC Church Mbeya', -8.933118, 33.418073, 'VERIFIED', true),
(3, 30, 'Masjid Nasir Ahmadiyya', 'Masjid Nasir Ahmadiyya Mosque', 'Nasir Mosque, Ahmadiyya Mosque', 'MUST-MOSQUE-AHM', 'Masjid Nasir Ahmadiyya Mosque', -8.901608, 33.434384, 'VERIFIED', true),
(3, 30, 'Mbeya Mosque', 'Mbeya Main Mosque', 'Mbeya Mosque', 'MUST-MOSQUE-MAIN', 'Mbeya Central Mosque', -8.896891, 33.435918, 'VERIFIED', true);

-- CATEGORY 31: SURROUNDING INFRASTRUCTURE, STREETS & LOCAL GOVERNANCE
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(3, 31, 'Mbeya University of Science and Technology (General/Main Gate)', 'Mbeya University Main Entrance Gate', 'Main Gate, MUST Gate', 'MUST-GATE-MAIN', 'MUST General Main Entrance Gate', -8.941950, 33.416060, 'VERIFIED', true),
(3, 31, 'Ikuti', 'Ikuti Junction & Area', 'Ikuti', 'MUST-IKUTI', 'Ikuti local area junction and landmark', -8.941431, 33.416390, 'VERIFIED', true),
(3, 31, 'Lema Street', 'Lema Street', 'Lema St', 'MUST-LEMA-ST', 'Lema Street nearby MUST campus', -8.940193, 33.418108, 'VERIFIED', true),
(3, 31, 'Nets', 'Nets Landmark Point', 'Nets', 'MUST-NETS', 'Nets area landmark near student hosteling zone', -8.941597, 33.413944, 'VERIFIED', true),
(3, 31, 'Ofisi ya Serikali ya Mtaa Sisitila (Mbeya Jiji)', 'Ofisi ya Serikali ya Mtaa Sisitila (Mbeya Jiji)', 'Sisitila Ward Office', 'MUST-MTAA-SISITILA', 'Sisitila Local Government Ward Office (Mbeya City)', -8.940189, 33.414620, 'VERIFIED', true);

-- ============================================================================
-- BEGIN SUA SPECIFIC DATA (From Flyway V15)
-- ============================================================================
DELETE FROM campus_location WHERE university_id = 4;
DELETE FROM category WHERE university_id = 4;

INSERT INTO category (id, university_id, name, slug, icon_name, description, is_active, sort_order) VALUES
(32, 4, 'Academic Units & Colleges', 'sua-colleges', 'school', 'Colleges, Faculties and Institutes', true, 1),
(33, 4, 'Academic Departments & Institutes', 'sua-departments', 'account_balance', 'Academic Departments and Specialized Institutes', true, 2),
(34, 4, 'Laboratories, Research, Farms & Field Stations', 'sua-research-farms', 'science', 'Laboratories, Research Centers, Farms and Field Stations', true, 3),
(35, 4, 'Administrative Offices, Security & Utilities', 'sua-administration', 'business', 'Main Administration, Security, ICT and Infrastructure', true, 4),
(36, 4, 'Student Accommodation & Residential Areas', 'sua-hostels', 'hotel', 'Student Hostels and Residential Quarters', true, 5),
(37, 4, 'Health & Student Welfare Services', 'sua-health', 'medical_services', 'SUA Health Center and Veterinary Teaching Hospital', true, 6),
(38, 4, 'Libraries, Lecture Halls & Educational Resources', 'sua-libraries-halls', 'menu_book', 'Libraries, Lecture Theatres and Learning Spaces', true, 7),
(39, 4, 'Sports, Recreation & Social Amenities', 'sua-sports-social', 'sports_soccer', 'Gymnasiums, Sports Grounds, Clubs and Halls', true, 8),
(40, 4, 'External Institutions, Schools & Commercial Services', 'sua-commercial-external', 'shopping_cart', 'Commercial Outlets, Banks, Schools and Partner Agencies', true, 9),
(41, 4, 'Religious Facilities', 'sua-religious', 'place', 'Chapels, Mosques and Religious Centers', true, 10),
(42, 4, 'Other Landmarks & Projects', 'sua-landmarks', 'park', 'Botanic Gardens, Water Projects and Field Initiatives', true, 11);

-- CATEGORY 32: ACADEMIC UNITS & COLLEGES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(4, 32, 'College of Agriculture', 'College of Agriculture', 'ColAg', 'SUA-CAGRI', 'Primary agricultural education and research college at SUA', -6.852724, 37.657167, 'VERIFIED', true),
(4, 32, 'College of Veterinary Medicine and Biomedical Sciences', 'College of Veterinary Medicine and Biomedical Sciences', 'CVMBS', 'SUA-CVMBS', 'Veterinary medicine, animal health, and biomedical sciences college', -6.849968, 37.658312, 'VERIFIED', true),
(4, 32, 'College of Social Sciences and Humanities', 'College of Social Sciences and Humanities', 'CSSH', 'SUA-CSSH', 'Social sciences, development studies, and humanities college', -6.852250, 37.659104, 'VERIFIED', true),
(4, 32, 'College of Forestry, Wildlife and Tourism', 'College of Forestry, Wildlife and Tourism', 'CFWT', 'SUA-CFWT', 'Forestry, natural resources, wildlife management, and tourism college', -6.852785, 37.658637, 'VERIFIED', true),
(4, 32, 'Institute of Continuing Education (ICE)', 'Institute of Continuing Education', 'ICE', 'SUA-ICE', 'Outreach, professional development, and continuing education institute', -6.896221, 37.658384, 'VERIFIED', true),
(4, 32, 'ACE II SUA', 'Africa Center of Excellence (ACE II) SUA', 'ACE II', 'SUA-ACE2', 'African Center of Excellence for Agro-Ecology and Food Security', -6.841950, 37.657411, 'VERIFIED', true);

-- CATEGORY 33: ACADEMIC DEPARTMENTS & INSTITUTES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(4, 33, 'Department of Food Science', 'Department of Food Science and Agro-Processing', 'Food Science', 'SUA-DFS', 'Food processing, nutrition, and technology department', -6.852213, 37.657652, 'VERIFIED', true),
(4, 33, 'Department of Development and Strategic Studies', 'Department of Development and Strategic Studies', 'DDSS', 'SUA-DDSS', 'Development studies and policy strategic research department', -6.852307, 37.657386, 'VERIFIED', true),
(4, 33, 'Department of Soil & Geological Sciences', 'Department of Soil & Geological Sciences', 'Soil Science', 'SUA-DSGS', 'Soil chemistry, physics, classification, and geology department', -6.853000, 37.657196, 'VERIFIED', true),
(4, 33, 'Department of Ecosystems and Conservation', 'Department of Ecosystems and Conservation', 'Ecosystems Dept', 'SUA-DEC', 'Biodiversity, ecosystem management, and conservation department', -6.853714, 37.657637, 'VERIFIED', true),
(4, 33, 'Department of Crop Science and Horticulture', 'Department of Crop Science and Horticulture', 'Crop Science', 'SUA-DCSH', 'Agronomy, horticulture, and plant breeding department', -6.853717, 37.657683, 'VERIFIED', true),
(4, 33, 'Agriculture Engineering & Land Planning', 'Department of Agricultural Engineering & Land Planning', 'Agro Eng', 'SUA-AELP', 'Agricultural machinery, irrigation, and land planning department', -6.853846, 37.657834, 'VERIFIED', true),
(4, 33, 'Department of Policy Planning and Management', 'Department of Policy Planning and Management', 'PPM', 'SUA-DPPM', 'Public policy, governance, and agricultural planning department', -6.854331, 37.657893, 'VERIFIED', true),
(4, 33, 'SUA Animal Science Dept', 'Department of Animal, Aquaculture and Range Sciences', 'Animal Science', 'SUA-DAS', 'Animal nutrition, breeding, aquaculture, and range management', -6.844898, 37.659374, 'VERIFIED', true),
(4, 33, 'Sokoine University of Agriculture Engineering', 'SUA School of Engineering and Technology', 'SUA Engineering', 'SUA-ENG', 'Engineering classrooms, workshops, and faculty offices', -6.843778, 37.658028, 'VERIFIED', true),
(4, 33, 'Department of Wildlife Management', 'Department of Wildlife Management', 'Wildlife Dept', 'SUA-DWM', 'Wildlife ecology, conservation, and protected area management', -6.852594, 37.659355, 'VERIFIED', true),
(4, 33, 'Department of Forest Engineering and Wood Sciences', 'Department of Forest Engineering and Wood Sciences', 'Forest Eng', 'SUA-DFEWS', 'Timber technology, forest engineering, and wood science department', -6.853414, 37.659037, 'VERIFIED', true),
(4, 33, 'Tanzania Official Seed Certification Institute', 'Tanzania Official Seed Certification Institute (TOSCI)', 'TOSCI', 'SUA-TOSCI', 'National seed quality control and certification agency office', -6.848990, 37.658060, 'VERIFIED', true);

-- CATEGORY 34: LABORATORIES, RESEARCH, FARMS & FIELD STATIONS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(4, 34, 'Soil Science Laboratory', 'Soil Science Central Laboratory', 'Soil Lab', 'SUA-LAB-SOIL', 'Central soil testing and analysis laboratory', -6.853662, 37.657089, 'VERIFIED', true),
(4, 34, 'Zoology Lab', 'SUA Zoology and Biological Sciences Laboratory', 'Zoology Lab', 'SUA-LAB-ZOO', 'Animal biology and zoology teaching laboratory', -6.854989, 37.656972, 'VERIFIED', true),
(4, 34, 'SUA Multipurpose Laboratory', 'SUA Central Multipurpose Laboratory Building', 'Multipurpose Lab', 'SUA-LAB-MULTI', 'Modern advanced science research laboratory facility', -6.842654, 37.656769, 'VERIFIED', true),
(4, 34, 'SUA New Lab', 'SUA New Science Laboratory Complex', 'New Lab', 'SUA-LAB-NEW', 'Newly constructed laboratory complex', -6.842584, 37.656817, 'VERIFIED', true),
(4, 34, 'Pest Management Centre', 'SUA Rodent and Pest Management Research Centre', 'Pest Center', 'SUA-PMC', 'National rodent and agricultural pest research center', -6.844896, 37.658126, 'VERIFIED', true),
(4, 34, 'Bioprocess and Post-Harvest Engineering', 'Bioprocess and Post-Harvest Technology Center', 'Post Harvest', 'SUA-BPHE', 'Agricultural crop processing and post-harvest technology center', -6.843530, 37.657523, 'VERIFIED', true),
(4, 34, 'Fish Production', 'SUA Aquaculture & Fish Production Unit', 'Fish Ponds', 'SUA-FISH', 'Aquaculture research ponds and fish hatchery unit', -6.852647, 37.650455, 'VERIFIED', true),
(4, 34, 'Magadu Animal Farm Bridge', 'Magadu Livestock Training Farm Bridge', 'Magadu Bridge', 'SUA-MAGADU-BRG', 'Bridge entry to Magadu livestock research farm', -6.852597, 37.653559, 'VERIFIED', true),
(4, 34, 'Model Training Farm (Slope)', 'SUA Model Agronomic Training Farm', 'Model Farm', 'SUA-FARM-MODEL', 'Practical slope agronomy and crop training farm', -6.852334, 37.653883, 'VERIFIED', true),
(4, 34, 'Horticulture Section', 'SUA Horticultural Research and Demonstration Section', 'Horticulture Farm', 'SUA-HORT', 'Vegetable and fruit research plots and greenhouses', -6.848948, 37.658594, 'VERIFIED', true),
(4, 34, 'Protected Farm Unit', 'SUA Protected Agriculture & Greenhouse Unit', 'Greenhouse Unit', 'SUA-PFU', 'Controlled environment agriculture and greenhouse research', -6.848460, 37.657509, 'VERIFIED', true),
(4, 34, 'National Carbon Monitoring Centre (NCMC)', 'National Carbon Monitoring Centre', 'NCMC', 'SUA-NCMC', 'Tanzania national carbon stock monitoring and research center', -6.851343, 37.659288, 'VERIFIED', true);

-- CATEGORY 35: ADMINISTRATIVE OFFICES, SECURITY & UTILITIES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(4, 35, 'Sokoine University of Agriculture (Main)', 'Sokoine University of Agriculture Central Landmark', 'SUA Main', 'SUA-MAIN-ENTRY', 'Central POI landmark for Sokoine University of Agriculture', -6.852027, 37.657591, 'VERIFIED', true),
(4, 35, 'SUA Administration Block', 'SUA Vice Chancellor & Central Administration Building', 'Admin Block', 'SUA-ADMIN', 'Central administrative headquarters and Vice Chancellor office', -6.852247, 37.657738, 'VERIFIED', true),
(4, 35, 'SUA ICT Lab', 'SUA Center for Information & Communication Technology', 'ICT Center', 'SUA-ICT', 'Main campus ICT infrastructure and server center', -6.852228, 37.657565, 'VERIFIED', true),
(4, 35, 'SUA Main Gate', 'Sokoine University Main Gate Entrance', 'Main Gate', 'SUA-GATE-MAIN', 'Primary security entrance gate to SUA main campus', -6.840501, 37.653751, 'VERIFIED', true),
(4, 35, 'SUA Police Station', 'SUA Campus Police Post', 'Police Post', 'SUA-POLICE', 'Campus security and Tanzania Police Force station', -6.828219, 37.664124, 'VERIFIED', true),
(4, 35, 'Estate Department SUA', 'SUA Estates & Works Department', 'Estate Dept', 'SUA-ESTATE', 'Campus maintenance, physical planning, and infrastructure department', -6.841342, 37.654617, 'VERIFIED', true),
(4, 35, 'SUALISA Offices', 'SUA Library and Information Services Administration', 'SUALISA', 'SUA-SUALISA', 'Library system administrative offices', -6.851370, 37.659438, 'VERIFIED', true),
(4, 35, 'SUA Post Office', 'SUA Campus Post Office', 'Post Office', 'SUA-POST', 'Postal and mailing services for campus community', -6.851922, 37.655468, 'VERIFIED', true),
(4, 35, 'Communication Tower', 'SUA Telecom & Cellular Communication Mast', 'Telecom Tower', 'SUA-TOWER', 'Central telecommunication signal tower', -6.850732, 37.657277, 'VERIFIED', true),
(4, 35, 'Motorcycle Stand', 'SUA Boda Boda & Transport Stand', 'Boda Stand', 'SUA-BODA', 'Designated motorcycle transport stand', -6.852344, 37.656255, 'VERIFIED', true);

-- CATEGORY 36: STUDENT ACCOMMODATION & RESIDENTIAL AREAS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(4, 36, 'SUA Visitors Hostel', 'SUA Central Visitors Guest House', 'Visitors Hostel', 'SUA-HST-VISIT', 'Accommodation for university guests and visiting scholars', -6.852848, 37.657552, 'VERIFIED', true),
(4, 36, 'ICE Hostel', 'Institute of Continuing Education Hostel', 'ICE Hostel', 'SUA-HST-ICE', 'Residential accommodation for short-course participants and students', -6.853938, 37.658421, 'VERIFIED', true),
(4, 36, 'Forest Visitors Hostel', 'Forestry Department Visitors Hostel', 'Forest Guest House', 'SUA-HST-FOR', 'Guest house for forestry researchers and visitors', -6.854861, 37.659395, 'VERIFIED', true),
(4, 36, 'Hostel 2', 'SUA Student Hostel Block 2', 'Hostel 2', 'SUA-HST-2', 'Student residential hall block 2', -6.853116, 37.656100, 'VERIFIED', true),
(4, 36, 'Hostel 3', 'SUA Student Hostel Block 3', 'Hostel 3', 'SUA-HST-3', 'Student residential hall block 3', -6.852679, 37.656008, 'VERIFIED', true),
(4, 36, 'Hostel 4', 'SUA Student Hostel Block 4', 'Hostel 4', 'SUA-HST-4', 'Student residential hall block 4', -6.852947, 37.655526, 'VERIFIED', true),
(4, 36, 'Hostel 6', 'SUA Student Hostel Block 6', 'Hostel 6', 'SUA-HST-6', 'Student residential hall block 6', -6.852917, 37.654576, 'VERIFIED', true),
(4, 36, 'Hostel 7', 'SUA Student Hostel Block 7', 'Hostel 7', 'SUA-HST-7', 'Student residential hall block 7', -6.852749, 37.654371, 'VERIFIED', true),
(4, 36, 'Hostel 8', 'SUA Student Hostel Block 8', 'Hostel 8', 'SUA-HST-8', 'Student residential hall block 8', -6.853523, 37.654950, 'VERIFIED', true),
(4, 36, 'Hostel 9', 'SUA Student Hostel Block 9', 'Hostel 9', 'SUA-HST-9', 'Student residential hall block 9', -6.852493, 37.652487, 'VERIFIED', true),
(4, 36, 'Hostel 11', 'SUA Student Hostel Block 11', 'Hostel 11', 'SUA-HST-11', 'Student residential hall block 11', -6.852725, 37.651953, 'VERIFIED', true),
(4, 36, 'Hostel 12', 'SUA Student Hostel Block 12', 'Hostel 12', 'SUA-HST-12', 'Student residential hall block 12', -6.852038, 37.652906, 'VERIFIED', true),
(4, 36, 'Engineers'' Hostel', 'SUA Engineering Students'' Hostel', 'Engineers Hostel', 'SUA-HST-ENG', 'Residential hostel for engineering students', -6.863108, 37.652150, 'VERIFIED', true),
(4, 36, 'Mubagade''s Hostel', 'Mubagade Student Residence', 'Mubagade Hostel', 'SUA-HST-MUB', 'Private student residence hostel', -6.865161, 37.655187, 'VERIFIED', true),
(4, 36, 'Susan Students Residents', 'Susan Student Residence Quarters', 'Susan Residence', 'SUA-HST-SUSAN', 'Off-campus private student accommodation', -6.863370, 37.655939, 'VERIFIED', true),
(4, 36, 'Gaza', 'Gaza Student Residential Quarter', 'Gaza', 'SUA-HST-GAZA', 'Popular student residential area', -6.852772, 37.652306, 'VERIFIED', true),
(4, 36, 'Hostel za Mama Kiganga', 'Mama Kiganga Private Student Hostels', 'Mama Kiganga Hostel', 'SUA-HST-KIGANGA', 'Private student accommodation near campus', -6.859876, 37.656670, 'VERIFIED', true),
(4, 36, 'Prof. Kilonzo Hostel', 'Prof. Kilonzo Student Hostel', 'Prof Kilonzo Hostel', 'SUA-HST-KILONZO', 'Private student residential hostel', -6.860372, 37.657201, 'VERIFIED', true),
(4, 36, 'Prof. Ngomuo''s Hostel', 'Prof. Ngomuo Student Hostel', 'Prof Ngomuo Hostel', 'SUA-HST-NGOMUO', 'Private student hostel quarters', -6.861769, 37.656057, 'VERIFIED', true),
(4, 36, 'SUA Asami Hostels', 'SUA Asami Student Hostel Complex', 'Asami Hostels', 'SUA-HST-ASAMI', 'Student hostel complex near main entrance', -6.840602, 37.656118, 'VERIFIED', true),
(4, 36, 'PP Ntatua Affordable Shelters', 'PP Ntatua Affordable Student Housing', 'Ntatua Shelters', 'SUA-HST-NTATUA', 'Affordable student housing quarters', -6.861346, 37.655925, 'VERIFIED', true),
(4, 36, 'Chaplain House', 'SUA Campus Chaplain Residence', 'Chaplain House', 'SUA-CHAPLAIN', 'Residence house for campus chaplain', -6.854411, 37.654333, 'VERIFIED', true);

-- CATEGORY 37: HEALTH & STUDENT WELFARE SERVICES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(4, 37, 'SUA Health Center', 'SUA Main Campus Health Center Hospital', 'SUA Hospital', 'SUA-HEALTH', 'Primary health center and outpatient clinic for students and staff', -6.853609, 37.656484, 'VERIFIED', true),
(4, 37, 'SUA Teaching Animal Hospital', 'SUA Veterinary Teaching Hospital', 'Animal Hospital', 'SUA-VET-HOSP', 'Veterinary clinical care and teaching hospital facility', -6.849552, 37.658597, 'VERIFIED', true);

-- CATEGORY 38: LIBRARIES, LECTURE HALLS & EDUCATIONAL RESOURCES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(4, 38, 'Library Garden', 'SUA National Agricultural Library Garden', 'Library Garden', 'SUA-LIB-GARDEN', 'Outdoor reading and relaxation garden at main library', -6.852691, 37.657983, 'VERIFIED', true),
(4, 38, 'Reprint Room (Mini Forestry Library)', 'Reprint Room & Forestry Reference Collection', 'Mini Library', 'SUA-MINI-LIB', 'Forestry reference library and document reprint room', -6.854297, 37.657671, 'VERIFIED', true),
(4, 38, 'SUA New Lecture Theatre', 'SUA New Central Lecture Theatre Complex', 'New LT', 'SUA-LT-NEW', 'Modern high-capacity lecture theatre complex', -6.846782, 37.658494, 'VERIFIED', true),
(4, 38, 'FM / Stationary', 'SUA Printing, FM & Stationery Center', 'Stationery Shop', 'SUA-FM-STAT', 'Stationery, document printing, and facilities management office', -6.852169, 37.653818, 'VERIFIED', true);

-- CATEGORY 39: SPORTS, RECREATION & SOCIAL AMENITIES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(4, 39, 'SUA Main-Campus Main Gym', 'SUA Main Gymnasium & Fitness Center', 'SUA Gym', 'SUA-GYM', 'Indoor sports, weightlifting, and fitness gymnasium', -6.851645, 37.655227, 'VERIFIED', true),
(4, 39, 'SUA Basketball Ground', 'SUA Campus Basketball Courts', 'Basketball Ground', 'SUA-BB-GROUND', 'Outdoor basketball courts for sports and recreation', -6.850732, 37.657277, 'VERIFIED', true),
(4, 39, 'Football Playground', 'SUA Main Campus Football Stadium & Pitch', 'Main Pitch', 'SUA-PITCH-MAIN', 'Primary soccer stadium and athletics field', -6.844685, 37.666785, 'VERIFIED', true),
(4, 39, 'Magadu Play Ground', 'Magadu Sports & Football Field', 'Magadu Pitch', 'SUA-MAGADU-PITCH', 'Sports field located near Magadu campus area', -6.862671, 37.651673, 'VERIFIED', true),
(4, 39, 'Suasa Club', 'SUA Staff and Alumni Club', 'Suasa Club', 'SUA-CLUB', 'Social club, lounge, and dining facility for staff and guests', -6.854577, 37.656171, 'VERIFIED', true),
(4, 39, 'SUA Multipurpose Hall', 'SUA Central Multipurpose Assembly Hall', 'Assembly Hall', 'SUA-HALL-MULTI', 'Large hall for university ceremonies, exams, and cultural events', -6.851984, 37.655252, 'VERIFIED', true);

-- CATEGORY 40: EXTERNAL INSTITUTIONS, SCHOOLS & COMMERCIAL SERVICES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(4, 40, 'TMA (Tanzania Meteorological Authority)', 'Tanzania Meteorological Authority Station', 'TMA', 'SUA-TMA', 'National weather forecasting and meteorological monitoring office', -6.853869, 37.657915, 'VERIFIED', true),
(4, 40, 'Automatic Weather Station Morogoro', 'Automatic Weather Monitoring Station', 'AWS Morogoro', 'SUA-AWS', 'Automated weather recording station', -6.841576, 37.655393, 'VERIFIED', true),
(4, 40, 'Livestock Training Agency (LITA)', 'Livestock Training Agency - Morogoro Campus', 'LITA', 'SUA-LITA', 'National livestock vocational training institute campus', -6.845906, 37.665068, 'VERIFIED', true),
(4, 40, 'LITA Cafeteria', 'Livestock Training Agency Canteen', 'LITA Canteen', 'SUA-LITA-CAF', 'Dining cafeteria at LITA campus area', -6.846164, 37.664719, 'VERIFIED', true),
(4, 40, 'LITA Hostel (TMAIP)', 'LITA Student Residential Hostel', 'LITA Hostel', 'SUA-LITA-HST', 'Hostel accommodation at LITA', -6.846181, 37.664157, 'VERIFIED', true),
(4, 40, 'SUA Primary School', 'SUA Demonstration Primary School', 'SUA Primary', 'SUA-PRI-SCH', 'Campus primary school for staff children and local community', -6.842303, 37.656036, 'VERIFIED', true),
(4, 40, 'SUA Secondary School', 'SUA Demonstration Secondary School', 'SUA Secondary', 'SUA-SEC-SCH', 'Secondary education school operated on campus', -6.841311, 37.664434, 'VERIFIED', true),
(4, 40, 'SUA Kindergarten Chekechea', 'SUA Nursery & Kindergarten School', 'Chekechea', 'SUA-CHEKECHEA', 'Nursery and early childhood education school', -6.852456, 37.659891, 'VERIFIED', true),
(4, 40, 'SUA Recom Cafeteria', 'Recom Dining & Cafeteria Services', 'Recom Canteen', 'SUA-RECOM-CAF', 'Popular student and staff cafeteria', -6.852335, 37.654968, 'VERIFIED', true),
(4, 40, 'SUA Shopping Area (Madukani)', 'SUA Commercial Shopping Center (Madukani)', 'Madukani', 'SUA-SHOPPING', 'Main commercial strip with shops, canteens, and services', -6.852186, 37.653887, 'VERIFIED', true),
(4, 40, 'CRDB Bank', 'CRDB Bank SUA Campus Branch', 'CRDB', 'SUA-CRDB', 'CRDB commercial bank branch and ATM services', -6.851586, 37.658670, 'VERIFIED', true),
(4, 40, 'NMR', 'NMB Bank ATM / Branch Outlet', 'NMB', 'SUA-NMR', 'NMB commercial bank outlet and ATM center', -6.851562, 37.658749, 'VERIFIED', true);

-- CATEGORY 41: RELIGIOUS FACILITIES
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(4, 41, 'CCT - SUA Chapel', 'Christian Council of Tanzania SUA Chapel', 'CCT Chapel', 'SUA-CCT', 'Protestant and interdenominational Christian campus chapel', -6.854994, 37.651142, 'VERIFIED', true),
(4, 41, 'RC SUA Parish', 'Roman Catholic St. Thomas More SUA Parish', 'RC Parish', 'SUA-RC-PARISH', 'Roman Catholic church and parish center on campus', -6.853518, 37.650697, 'VERIFIED', true),
(4, 41, 'SUA SDA Church', 'Seventh-day Adventist Campus Church', 'SDA Church', 'SUA-SDA', 'Seventh-day Adventist campus church', -6.858789, 37.659858, 'VERIFIED', true),
(4, 41, 'Masjid Kididimo', 'Kididimo Campus Mosque', 'Kididimo Mosque', 'SUA-MOSQUE-KIDIDIMO', 'Islamic place of worship near Kididimo area', -6.861416, 37.653488, 'VERIFIED', true),
(4, 41, 'SUA Mosque', 'SUA Main Campus Central Mosque', 'Main Mosque', 'SUA-MOSQUE-MAIN', 'Central campus mosque for Islamic prayers', -6.840242, 37.653651, 'VERIFIED', true);

-- CATEGORY 42: OTHER LANDMARKS & PROJECTS
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(4, 42, 'SUA University Botanic Garden', 'SUA University Botanic Garden & Arboretum', 'Botanic Garden', 'SUA-BOTANIC', 'Conservation botanic garden and plant specimen collection', -6.841342, 37.654617, 'VERIFIED', true),
(4, 42, 'EMC 1 Store / EMC Eco Water Project', 'EMC Eco Water Project and Store Facility', 'Eco Water', 'SUA-EMC-WATER', 'Water recycling and environmental eco-project facility', -6.852785, 37.655199, 'VERIFIED', true),
(4, 42, 'SUGELO', 'SUA Agricultural Extension and Outreach Project', 'SUGELO', 'SUA-SUGELO', 'Agricultural extension project demonstration area', -6.839361, 37.652747, 'VERIFIED', true),
(4, 42, 'Shamba Initiative', 'Shamba Agricultural Innovation Initiative', 'Shamba Project', 'SUA-SHAMBA', 'Community farming innovation and sustainability project', -6.853502, 37.650480, 'VERIFIED', true),
(4, 42, 'Improving Beekeeping in Uluguru', 'Uluguru Mountain Beekeeping Improvement Project', 'Beekeeping Project', 'SUA-BEEKEEPING', 'Apiculture and honey production extension project center', -6.852587, 37.659466, 'VERIFIED', true),
(4, 42, 'Kididimo Magengeni', 'Kididimo Magengeni Landmark Area', 'Kididimo', 'SUA-KIDIDIMO', 'Geographical campus landmark near Kididimo', -6.860762, 37.654661, 'VERIFIED', true),
(4, 42, 'SUA Bridge', 'SUA Main Access Stream Bridge', 'Campus Bridge', 'SUA-BRIDGE', 'Bridge across campus stream', -6.847997, 37.663672, 'VERIFIED', true);

-- ============================================================================
-- SEED DEPARTMENTS & COMMUNITY INTELLIGENCE SYSTEM DATA
-- ============================================================================
INSERT INTO department (id, name, description) VALUES
(1, 'Water Authority', 'Responsible for water supply and infrastructure'),
(2, 'Roads Department', 'Management of public roads and potholes'),
(3, 'Waste Management', 'Garbage collection and illegal dumping'),
(4, 'Electricity Board', 'Electrical infrastructure and street lights'),
(5, 'Public Safety', 'Emergency response and security concerns'),
(6, 'Municipal Works', 'General maintenance and buildings');

INSERT INTO community_user (id, external_id, username, password_hash, full_name, email, role) VALUES
(1, 'USR-ADMIN-001', 'superuser', '$2a$10$8.UnVuG9HHgffUDAlk8qn.6nQH22LQuYBQkyJDdyt2WegB6.p7Nle', 'System Administrator', 'admin@campnav.org', 'ADMIN'),
(2, 'USR-OFF-001', 'facilitator', '$2a$10$8.UnVuG9HHgffUDAlk8qn.6nQH22LQuYBQkyJDdyt2WegB6.p7Nle', 'Field Facilitator', 'facilitator@campnav.org', 'FIELD_OFFICER'),
(3, 'USR-OFF-002', 'coordinator', '$2a$10$8.UnVuG9HHgffUDAlk8qn.6nQH22LQuYBQkyJDdyt2WegB6.p7Nle', 'Department Coordinator', 'coordinator@campnav.org', 'DEPT_OFFICER');

INSERT INTO issue_report (id, external_id, category, description, latitude, longitude, status, user_severity, calculated_priority, impact_score, reporter_id) VALUES
(1, 'JMF-2026-000184', 'WATER', 'Major water pipe leakage near the student hostel. Water is flooding the walkway.', -6.7924, 39.2083, 'IN_PROGRESS', 'HIGH', 'CRITICAL', 85, 1),
(2, 'JMF-2026-000185', 'ROADS', 'Large pothole on the main university entrance road, dangerous for motorcycles.', -6.7801, 39.2041, 'ASSIGNED', 'MEDIUM', 'HIGH', 70, 1),
(3, 'JMF-2026-000186', 'WASTE', 'Illegal garbage dumping behind the cafeteria. Health hazard.', -6.7770, 39.2025, 'SUBMITTED', 'MEDIUM', 'MEDIUM', 45, 1),
(4, 'JMF-2026-000187', 'OTHER', 'Noise complaint near study area. Constant loud music from nearby construction.', -6.7810, 39.2050, 'SUBMITTED', 'LOW', 'LOW', 20, 1);

INSERT INTO issue_evidence (issue_id, evidence_url) VALUES
(1, 'https://images.unsplash.com/photo-1590483734724-38fa19dd780c?auto=format&fit=crop&w=400&q=80'),
(1, 'https://images.unsplash.com/photo-1518709766631-a6a7f4593b3e?auto=format&fit=crop&w=400&q=80'),
(2, 'https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?auto=format&fit=crop&w=400&q=80'),
(3, 'https://images.unsplash.com/photo-1530587191325-3db32d826c18?auto=format&fit=crop&w=400&q=80');

UPDATE issue_report SET voice_url = 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3' WHERE external_id = 'JMF-2026-000187';

INSERT INTO issue_timeline (issue_id, status, message) VALUES
(1, 'SUBMITTED', 'Issue reported by citizen'),
(1, 'VERIFIED', 'Location verified via GPS'),
(1, 'ASSIGNED', 'Assigned to Water Works Dept'),
(1, 'IN_PROGRESS', 'Officer on site, repair started'),
(2, 'SUBMITTED', 'Issue reported by citizen'),
(2, 'ASSIGNED', 'Assigned to Roads Dept'),
(3, 'SUBMITTED', 'Issue reported by citizen');

-- ============================================================================
-- SYNC SEQUENCES FOR POSTGRESQL / H2
-- ============================================================================
ALTER TABLE university ALTER COLUMN id RESTART WITH 10;
ALTER TABLE campus ALTER COLUMN id RESTART WITH 10;
ALTER TABLE category ALTER COLUMN id RESTART WITH 60;
ALTER TABLE department ALTER COLUMN id RESTART WITH 10;
ALTER TABLE community_user ALTER COLUMN id RESTART WITH 10;
ALTER TABLE issue_report ALTER COLUMN id RESTART WITH 10;
