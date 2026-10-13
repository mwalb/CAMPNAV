-- Migration V22: Add custom_category column to community_contribution
ALTER TABLE community_contribution ADD COLUMN IF NOT EXISTS custom_category VARCHAR(255);
