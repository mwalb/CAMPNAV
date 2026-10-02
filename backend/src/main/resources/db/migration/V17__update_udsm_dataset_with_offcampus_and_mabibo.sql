-- Migration V17: Update University of Dar es Salaam (UDSM) locations with verified coordinates and off-campus/Mabibo entries

-- Update existing Nkrumah Hall coordinates
UPDATE campus_location
SET latitude = -6.780925, longitude = 39.204594, category_id = 2, verification_status = 'VERIFIED'
WHERE university_id = 1 AND (name ILIKE '%Nkrumah Hall%' OR official_name ILIKE '%Nkrumah Hall%') AND category_id = 2;

-- Update existing Hall 3 residency coordinates
UPDATE campus_location
SET latitude = -6.774677, longitude = 39.207443, name = 'Hall 3 (Block B / Block 113)', official_name = 'Hall 3 Block B (Block 113)', verification_status = 'VERIFIED'
WHERE university_id = 1 AND (name ILIKE '%Hall 3%' OR official_name ILIKE '%Hall 3%') AND category_id = 4;

-- Delete old duplicate or conflicting entries if present to prevent duplicate records
DELETE FROM campus_location WHERE university_id = 1 AND name IN (
    'USAB',
    'Ofisi ya Bodi ya Mikopo (HESLB Office)',
    'CoICT',
    'SJMC',
    'Mabibo Hostel (Main Entrance)',
    'Mabibo Hostel Block A',
    'Mabibo Hostel Block C',
    'Mabibo Hostel Football Ground',
    'Mabibo Hostel Basketball Court'
);

-- Insert new requested verified locations for UDSM (University ID 1)

-- 1. Main Campus Academic & Administrative Offices
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(1, 5, 'USAB', 'University Student Accommodation Bureau', 'USAB; Accommodation Bureau; Hostel Management Office', 'USAB', 'University Student Accommodation Bureau office', -6.776476, 39.207032, 'VERIFIED', true),
(1, 5, 'Ofisi ya Bodi ya Mikopo (HESLB Office)', 'Higher Education Students Loans Board Office (HESLB)', 'HESLB; Bodi ya Mikopo; Loan Board Office', 'HESLB', 'Higher Education Students Loans Board (HESLB) helpdesk & office', -6.849142, 39.249344, 'VERIFIED', true);

-- 2. Off-Campus Colleges & Schools
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(1, 1, 'CoICT', 'College of Information and Communication Technologies', 'CoICT; Sayansi Campus; Kijitonyama Campus; ICT College', 'CoICT', 'College of Information and Communication Technologies at Kijitonyama Sayansi', -6.771471, 39.239928, 'VERIFIED', true),
(1, 1, 'SJMC', 'School of Journalism and Mass Communication', 'SJMC; Journalism School; Mikocheni Campus', 'SJMC', 'School of Journalism and Mass Communication at Mikocheni', -6.770842, 39.250056, 'VERIFIED', true);

-- 3. Mabibo Hostels Complex
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(1, 4, 'Mabibo Hostel (Main Entrance)', 'Mabibo Hostels Complex Main Entrance', 'Mabibo Hostel; Mabibo Complex; Mabibo Main Gate', 'MABIBO', 'Mabibo Hostels main entrance and general complex landmark', -6.805792, 39.208399, 'VERIFIED', true),
(1, 4, 'Mabibo Hostel Block A', 'Mabibo Hostel Block A Residency', 'Mabibo Block A; Block A', 'MAB-A', 'Mabibo Hostel Block A student residence', -6.803610, 39.206607, 'VERIFIED', true),
(1, 4, 'Mabibo Hostel Block C', 'Mabibo Hostel Block C Residency', 'Mabibo Block C; Block C', 'MAB-C', 'Mabibo Hostel Block C student residence', -6.804374, 39.205730, 'VERIFIED', true);

-- 4. Sports & Recreation Facilities
INSERT INTO campus_location (university_id, category_id, name, official_name, aliases, building_code, description, latitude, longitude, verification_status, is_active) VALUES
(1, 8, 'Mabibo Hostel Football Ground', 'Mabibo Hostel Football Ground', 'Mabibo Football Pitch; Mabibo Pitch; Mabibo Ground', 'MAB-FB', 'Football ground at Mabibo Hostels complex', -6.806000, 39.206980, 'VERIFIED', true),
(1, 8, 'Mabibo Hostel Basketball Court', 'Mabibo Hostel Basketball Ground', 'Mabibo Basketball Court; Mabibo BB Ground', 'MAB-BB', 'Basketball court at Mabibo Hostels complex', -6.805924, 39.207475, 'VERIFIED', true);
