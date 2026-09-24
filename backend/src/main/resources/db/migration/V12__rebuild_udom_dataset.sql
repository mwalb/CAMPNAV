-- Rebuild UDOM (University of Dodoma) dataset with 11 categories and 169 verified campus locations

-- 1. ENSURE UDOM UNIVERSITY RECORD EXISTS (University ID 2)
INSERT INTO university (id, external_id, name, official_name, short_name, city, country, latitude, longitude, default_zoom, description, logo_url, sort_order, campus_area_hectares, is_active, status)
SELECT 2, 'UDOM', 'University of Dodoma', 'University of Dodoma', 'UDOM', 'Dodoma', 'Tanzania', -6.2033, 35.8000, 14.0, 'Embracing Knowledge', 'https://upload.wikimedia.org/wikipedia/en/1/1b/UDOM_Logo.png', 2, 6000.0, true, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM university WHERE id = 2 OR external_id = 'UDOM');

-- Ensure campus record exists for UDOM
INSERT INTO campus (id, external_id, university_id, name)
SELECT 2, 'UDOM-MAIN', 2, 'University of Dodoma Main Campus'
WHERE NOT EXISTS (SELECT 1 FROM campus WHERE id = 2 OR external_id = 'UDOM-MAIN');

-- 2. CLEANUP EXISTING UDOM DATA
DELETE FROM campus_location WHERE university_id = 2;
DELETE FROM category WHERE university_id = 2;

-- 3. INSERT 11 CATEGORIES FOR UDOM (University ID 2)
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

-- 4. INSERT UDOM LOCATIONS WITH VERIFIED COORDINATES

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
