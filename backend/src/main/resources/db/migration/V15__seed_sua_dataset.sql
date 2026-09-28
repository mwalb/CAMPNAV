-- Seed SUA (Sokoine University of Agriculture) dataset with 11 categories and 82 verified campus locations

-- 1. ENSURE SUA UNIVERSITY RECORD EXISTS (University ID 4)
INSERT INTO university (id, external_id, name, official_name, short_name, city, country, latitude, longitude, default_zoom, description, logo_url, sort_order, is_active, status)
SELECT 4, 'SUA', 'Sokoine University of Agriculture', 'Sokoine University of Agriculture', 'SUA', 'Morogoro', 'Tanzania', -6.8475, 37.6591, 15.0, 'Ardhi ni Hazina', 'https://upload.wikimedia.org/wikipedia/en/3/3d/Sua_logo.png', 4, true, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM university WHERE id = 4 OR external_id = 'SUA');

-- Ensure campus record exists for SUA
INSERT INTO campus (id, external_id, university_id, name)
SELECT 4, 'SUA-MAIN', 4, 'Sokoine University of Agriculture Main Campus'
WHERE NOT EXISTS (SELECT 1 FROM campus WHERE id = 4 OR external_id = 'SUA-MAIN');

-- 2. CLEANUP EXISTING SUA DATA
DELETE FROM campus_location WHERE university_id = 4;
DELETE FROM category WHERE university_id = 4;

-- 3. INSERT CATEGORIES FOR SUA (University ID 4)
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

-- 4. INSERT SUA LOCATIONS WITH VERIFIED COORDINATES

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
(4, 36, 'Hostel za Mama Kiganga', 'Mama Kiganga Private Student Hostels', 'Mama Kiganga Hostel', 'SUA-HST-KIGANGA', 'Private student accommodation near campus', -6.859876, 37.65667, 'VERIFIED', true),
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

-- SYNC SEQUENCES FOR H2 / POSTGRESQL
ALTER TABLE university ALTER COLUMN id RESTART WITH 10;
ALTER TABLE campus ALTER COLUMN id RESTART WITH 10;
ALTER TABLE category ALTER COLUMN id RESTART WITH 60;
