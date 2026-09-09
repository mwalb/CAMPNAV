ALTER TABLE category ADD COLUMN slug VARCHAR(100);
ALTER TABLE category ADD COLUMN description TEXT;
ALTER TABLE category ADD COLUMN is_active BOOLEAN DEFAULT TRUE;
ALTER TABLE category ADD COLUMN sort_order INTEGER;
ALTER TABLE category ADD COLUMN created_at TIMESTAMP;
ALTER TABLE category ADD COLUMN updated_at TIMESTAMP;

-- Update existing categories with slugs
UPDATE category SET slug = LOWER(REPLACE(name, ' ', '-')) WHERE slug IS NULL;
