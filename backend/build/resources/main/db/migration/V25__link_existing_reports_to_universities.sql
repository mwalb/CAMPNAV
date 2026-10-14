-- Migration V25: Associate existing reports with universities where relationship can be established reliably

-- Link reports around UDSM (lat -6.85 to -6.75, lng 39.15 to 39.30) to UDSM (university_id = 1)
UPDATE issue_report
SET university_id = 1
WHERE university_id IS NULL AND latitude BETWEEN -6.85 AND -6.75 AND longitude BETWEEN 39.15 AND 39.30;

-- Link reports around UDOM (lat -6.25 to -6.10, lng 35.70 to 35.90) to UDOM (university_id = 2)
UPDATE issue_report
SET university_id = 2
WHERE university_id IS NULL AND latitude BETWEEN -6.25 AND -6.10 AND longitude BETWEEN 35.70 AND 35.90;

-- Link reports around MUST (lat -9.00 to -8.85, lng 33.35 to 33.50) to MUST (university_id = 3)
UPDATE issue_report
SET university_id = 3
WHERE university_id IS NULL AND latitude BETWEEN -9.00 AND -8.85 AND longitude BETWEEN 33.35 AND 33.50;

-- Link reports around SUA (lat -6.90 to -6.80, lng 37.60 to 37.70) to SUA (university_id = 4)
UPDATE issue_report
SET university_id = 4
WHERE university_id IS NULL AND latitude BETWEEN -6.90 AND -6.80 AND longitude BETWEEN 37.60 AND 37.70;
