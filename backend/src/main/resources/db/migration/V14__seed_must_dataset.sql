-- Seed MUST (Mbeya University of Science and Technology) dataset with 7 categories and 37 verified campus locations

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
(25, 3, 'Academic & Administrative', 'must-academic-admin', 'school', 'Academic faculties, lecture halls, laboratories, workshops, libraries, and administrative offices', true, 1),
(26, 3, 'Student Accommodation & Housing Blocks', 'must-hostel', 'hotel', 'Student hostels, residential halls, and housing blocks', true, 2),
(27, 3, 'Health & Campus Services', 'must-health-services', 'medical_services', 'Dispensaries, health centers, financial services, and campus stores', true, 3),
(28, 3, 'Commercial, Dining & Shops', 'must-commercial-dining', 'shopping_cart', 'Cafes, restaurants, pubs, shops, internet services, and commercial establishments', true, 4),
(29, 3, 'Sports & Recreation', 'must-sports', 'sports_soccer', 'Football grounds, playgrounds, and recreational sports facilities', true, 5),
(30, 3, 'Places of Worship', 'must-religious', 'account_balance', 'Churches, mosques, and places of worship in and around MUST', true, 6),
(31, 3, 'Surrounding Infrastructure, Streets & Local Governance', 'must-infrastructure', 'place', 'Main gates, streets, local governance offices, and surrounding landmarks', true, 7);

-- 4. INSERT MUST LOCATIONS WITH VERIFIED COORDINATES FROM HANDWRITTEN LOGS

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

-- SYNC SEQUENCES FOR H2 / POSTGRESQL
ALTER TABLE category ALTER COLUMN id RESTART WITH 60;
