-- Migration V18: Update Nkrumah Hall coordinates, update SUA NMR to NMB, and create community contribution, review, and audit log tables

-- 1. Update UDSM Nkrumah Hall coordinates
UPDATE campus_location
SET latitude = -6.780543, longitude = 39.204593, verification_status = 'VERIFIED'
WHERE university_id = 1 AND (name ILIKE '%Nkrumah Hall%' OR official_name ILIKE '%Nkrumah Hall%');

-- 2. Update SUA NMR to NMB
UPDATE campus_location
SET name = 'NMB', official_name = 'NMB Bank ATM / Branch Outlet', verification_status = 'VERIFIED'
WHERE university_id = 4 AND (name ILIKE '%NMR%' OR building_code ILIKE '%NMR%' OR aliases ILIKE '%NMR%');

-- 3. Create community contribution table
CREATE TABLE IF NOT EXISTS community_contribution (
    id BIGSERIAL PRIMARY KEY,
    reference VARCHAR(50) UNIQUE NOT NULL,
    university_id BIGINT REFERENCES university(id) ON DELETE CASCADE,
    location_name VARCHAR(255) NOT NULL,
    area_type VARCHAR(50) NOT NULL, -- 'UNIVERSITY_AREA' or 'COMMERCIAL_AREA'
    category_id BIGINT REFERENCES category(id),
    description TEXT,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    contributor_name VARCHAR(255) NOT NULL,
    contributor_contact VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    payment_status VARCHAR(50) NOT NULL DEFAULT 'PAYMENT_NOT_REQUIRED', -- PAYMENT_NOT_REQUIRED, PAYMENT_PENDING, PAYMENT_PAID, PAYMENT_FAILED, PAYMENT_REFUNDED
    rejection_reason TEXT,
    submitted_by_user_id BIGINT REFERENCES community_user(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by VARCHAR(255)
);

-- 4. Create community review table
CREATE TABLE IF NOT EXISTS community_review (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT REFERENCES university(id) ON DELETE CASCADE,
    location_id BIGINT REFERENCES campus_location(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT NOT NULL,
    contributor_name VARCHAR(255),
    contributor_contact VARCHAR(255),
    status VARCHAR(50) DEFAULT 'APPROVED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Create admin activity log table
CREATE TABLE IF NOT EXISTS admin_activity_log (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    actor_username VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    related_entity_id VARCHAR(100),
    ip_address VARCHAR(100),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_community_contribution_status ON community_contribution(status);
CREATE INDEX IF NOT EXISTS idx_community_contribution_university ON community_contribution(university_id);
CREATE INDEX IF NOT EXISTS idx_community_review_university ON community_review(university_id);
CREATE INDEX IF NOT EXISTS idx_community_review_location ON community_review(location_id);
CREATE INDEX IF NOT EXISTS idx_admin_activity_log_timestamp ON admin_activity_log(timestamp);
