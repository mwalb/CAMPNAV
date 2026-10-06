-- Migration V20: Add location source, internal notes, and public response to issue_report table

ALTER TABLE issue_report ADD COLUMN IF NOT EXISTS location_source VARCHAR(50);
ALTER TABLE issue_report ADD COLUMN IF NOT EXISTS internal_notes TEXT;
ALTER TABLE issue_report ADD COLUMN IF NOT EXISTS public_response TEXT;
