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
