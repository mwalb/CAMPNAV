-- COMMUNITY ISSUE REPORTING SCHEMA
-- Modern, scalable and technically advanced community intelligence system

-- 1. DEPARTMENTS
CREATE TABLE department (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    head_officer_id VARCHAR(50),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. COMMUNITY USERS
CREATE TABLE community_user (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(100) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(50),
    role VARCHAR(50) DEFAULT 'CITIZEN', -- CITIZEN, FIELD_OFFICER, DEPT_OFFICER, ADMIN
    department_id BIGINT REFERENCES department(id),
    reputation_score INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- 3. ISSUE REPORTS
CREATE TABLE issue_report (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(50) UNIQUE NOT NULL, -- JMF-2026-000184
    category VARCHAR(50) NOT NULL,
    sub_category VARCHAR(100),
    description TEXT NOT NULL,
    voice_transcription TEXT,

    -- Location Intelligence
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    address TEXT,
    street VARCHAR(255),
    ward VARCHAR(100),
    district VARCHAR(100),
    region VARCHAR(100),
    nearby_landmark VARCHAR(255),

    reporter_latitude DOUBLE PRECISION,
    reporter_longitude DOUBLE PRECISION,
    distance_to_issue DOUBLE PRECISION, -- in meters
    location_accuracy FLOAT,

    -- Content
    video_url TEXT,
    voice_url TEXT,
    before_image_url TEXT,
    after_image_url TEXT,

    -- Status & Priority
    status VARCHAR(50) DEFAULT 'SUBMITTED',
    user_severity VARCHAR(20), -- LOW, MEDIUM, HIGH, CRITICAL
    calculated_priority VARCHAR(20),
    impact_score INTEGER DEFAULT 0,
    is_emergency BOOLEAN DEFAULT FALSE,

    -- Assignment
    reporter_id BIGINT REFERENCES community_user(id),
    assigned_department_id BIGINT REFERENCES department(id),
    assigned_officer_id BIGINT REFERENCES community_user(id),

    -- Interaction
    supports_count INTEGER DEFAULT 0,
    verification_count INTEGER DEFAULT 0,

    -- Audit
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Supporting table for issue evidence (image URLs) - Required by JPA @ElementCollection
CREATE TABLE issue_evidence (
    issue_id BIGINT NOT NULL REFERENCES issue_report(id) ON DELETE CASCADE,
    evidence_url TEXT NOT NULL
);

-- 4. ISSUE COMMENTS & INTERACTION
CREATE TABLE issue_comment (
    id BIGSERIAL PRIMARY KEY,
    issue_id BIGINT REFERENCES issue_report(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES community_user(id),
    comment_text TEXT NOT NULL,
    is_official BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Supporting table for comment attachments - Required by JPA @ElementCollection
CREATE TABLE comment_attachment (
    comment_id BIGINT NOT NULL REFERENCES issue_comment(id) ON DELETE CASCADE,
    attachment_url TEXT NOT NULL
);

-- 5. ISSUE TIMELINE / LIFECYCLE
CREATE TABLE issue_timeline (
    id BIGSERIAL PRIMARY KEY,
    issue_id BIGINT REFERENCES issue_report(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    message TEXT,
    actor_id BIGINT REFERENCES community_user(id),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexing for performance
CREATE INDEX idx_issue_report_location ON issue_report(latitude, longitude);
CREATE INDEX idx_issue_report_status ON issue_report(status);
CREATE INDEX idx_issue_report_category ON issue_report(category);
CREATE INDEX idx_community_user_external_id ON community_user(external_id);

-- SEED DEPARTMENTS
INSERT INTO department (name, description) VALUES
('Water Authority', 'Responsible for water supply and infrastructure'),
('Roads Department', 'Management of public roads and potholes'),
('Waste Management', 'Garbage collection and illegal dumping'),
('Electricity Board', 'Electrical infrastructure and street lights'),
('Public Safety', 'Emergency response and security concerns'),
('Municipal Works', 'General maintenance and buildings');

-- SEED COMMUNITY USERS (CREDENTIALS)
-- Passwords are hashed: "password123" -> "$2a$10$8.UnVuG9HHgffUDAlk8qn.6nQH22LQuYBQkyJDdyt2WegB6.p7Nle"
INSERT INTO community_user (external_id, username, password_hash, full_name, email, role) VALUES
('USR-ADMIN-001', 'superuser', '$2a$10$8.UnVuG9HHgffUDAlk8qn.6nQH22LQuYBQkyJDdyt2WegB6.p7Nle', 'System Administrator', 'admin@campnav.org', 'ADMIN'),
('USR-OFF-001', 'facilitator', '$2a$10$8.UnVuG9HHgffUDAlk8qn.6nQH22LQuYBQkyJDdyt2WegB6.p7Nle', 'Field Facilitator', 'facilitator@campnav.org', 'FIELD_OFFICER'),
('USR-OFF-002', 'coordinator', '$2a$10$8.UnVuG9HHgffUDAlk8qn.6nQH22LQuYBQkyJDdyt2WegB6.p7Nle', 'Department Coordinator', 'coordinator@campnav.org', 'DEPT_OFFICER');
