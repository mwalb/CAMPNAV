-- SEED SOME COMMUNITY REPORTS WITH EVIDENCE
INSERT INTO issue_report (external_id, category, description, latitude, longitude, status, user_severity, calculated_priority, impact_score, reporter_id) VALUES
('JMF-2026-000184', 'WATER', 'Major water pipe leakage near the student hostel. Water is flooding the walkway.', -6.7924, 39.2083, 'IN_PROGRESS', 'HIGH', 'CRITICAL', 85, 1),
('JMF-2026-000185', 'ROADS', 'Large pothole on the main university entrance road, dangerous for motorcycles.', -6.7801, 39.2041, 'ASSIGNED', 'MEDIUM', 'HIGH', 70, 1),
('JMF-2026-000186', 'WASTE', 'Illegal garbage dumping behind the cafeteria. Health hazard.', -6.7770, 39.2025, 'SUBMITTED', 'MEDIUM', 'MEDIUM', 45, 1),
('JMF-2026-000187', 'OTHER', 'Noise complaint near study area. Constant loud music from nearby construction.', -6.7810, 39.2050, 'SUBMITTED', 'LOW', 'LOW', 20, 1);

-- EVIDENCE FOR REPORTS
INSERT INTO issue_evidence (issue_id, evidence_url) VALUES
(1, 'https://images.unsplash.com/photo-1590483734724-38fa19dd780c?auto=format&fit=crop&w=400&q=80'),
(1, 'https://images.unsplash.com/photo-1518709766631-a6a7f4593b3e?auto=format&fit=crop&w=400&q=80'),
(2, 'https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?auto=format&fit=crop&w=400&q=80'),
(3, 'https://images.unsplash.com/photo-1530587191325-3db32d826c18?auto=format&fit=crop&w=400&q=80');

-- UPDATE VOICE URL FOR NOISE COMPLAINT
UPDATE issue_report SET voice_url = 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3' WHERE external_id = 'JMF-2026-000187';

-- TIMELINE FOR REPORTS
INSERT INTO issue_timeline (issue_id, status, message) VALUES
(1, 'SUBMITTED', 'Issue reported by citizen'),
(1, 'VERIFIED', 'Location verified via GPS'),
(1, 'ASSIGNED', 'Assigned to Water Works Dept'),
(1, 'IN_PROGRESS', 'Officer on site, repair started'),
(2, 'SUBMITTED', 'Issue reported by citizen'),
(2, 'ASSIGNED', 'Assigned to Roads Dept'),
(3, 'SUBMITTED', 'Issue reported by citizen');
