-- Migration V23: Add reporter name, email, and phone fields to issue_report table
ALTER TABLE issue_report ADD COLUMN IF NOT EXISTS reporter_name VARCHAR(255);
ALTER TABLE issue_report ADD COLUMN IF NOT EXISTS reporter_email VARCHAR(255);
ALTER TABLE issue_report ADD COLUMN IF NOT EXISTS reporter_phone VARCHAR(255);
