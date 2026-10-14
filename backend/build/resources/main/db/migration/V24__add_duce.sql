-- Migration V24: Add Dar es Salaam University College of Education (DUCE) to University of Dar es Salaam (UDSM)

DELETE FROM campus_location
WHERE university_id = 1 AND (name = 'Dar es Salaam University College of Education' OR building_code = 'DUCE');

INSERT INTO campus_location (
    university_id,
    category_id,
    name,
    official_name,
    aliases,
    building_code,
    description,
    latitude,
    longitude,
    verification_status,
    is_active
)
SELECT
    1,
    1,
    'Dar es Salaam University College of Education',
    'Dar es Salaam University College of Education',
    'DUCE; Dar es Salaam University College of Education',
    'DUCE',
    'Dar es Salaam University College of Education constituent college of UDSM',
    -6.849535,
    39.273106,
    'VERIFIED',
    true
WHERE NOT EXISTS (
    SELECT 1 FROM campus_location
    WHERE university_id = 1 AND (name = 'Dar es Salaam University College of Education' OR building_code = 'DUCE')
);
