-- V26: Safe cleanup of demo reports seeded by V11 without modifying applied migration history
DELETE FROM issue_evidence WHERE issue_id IN (SELECT id FROM issue_report WHERE external_id LIKE 'JMF-2026-%');
DELETE FROM issue_timeline WHERE issue_id IN (SELECT id FROM issue_report WHERE external_id LIKE 'JMF-2026-%');
DELETE FROM issue_report WHERE external_id LIKE 'JMF-2026-%';
