-- Migration V21: University Student Leader Assignment, Email Notification Outbox Queue, and University Office Referrals

-- 1. Student Leader Assignment Table
CREATE TABLE IF NOT EXISTS student_leader_assignment (
    id BIGSERIAL PRIMARY KEY,
    leader_id BIGINT NOT NULL REFERENCES community_user(id) ON DELETE CASCADE,
    university_id BIGINT NOT NULL REFERENCES university(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED, DEACTIVATED, REPLACED
    appointed_by_admin_id BIGINT REFERENCES community_user(id),
    appointment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verification_date TIMESTAMP,
    start_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_date TIMESTAMP,
    deactivation_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Email Notification Outbox Table
CREATE TABLE IF NOT EXISTS email_notification (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT REFERENCES issue_report(id) ON DELETE CASCADE,
    recipient_email VARCHAR(255) NOT NULL,
    recipient_role VARCHAR(50) NOT NULL, -- LEADER, REPORTER, ADMIN
    notification_type VARCHAR(100) NOT NULL, -- NEW_REPORT_LEADER, REPORT_STATUS_UPDATE_REPORTER, MISSING_LEADER_ADMIN, DELIVERY_FAILURE_ADMIN, OVERDUE_ACK_ADMIN
    subject VARCHAR(255) NOT NULL,
    body_text TEXT NOT NULL,
    body_html TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    attempt_count INTEGER DEFAULT 0,
    last_attempt_at TIMESTAMP,
    next_retry_at TIMESTAMP,
    delivery_state VARCHAR(50) DEFAULT 'PENDING', -- PENDING, PROCESSING, SENT_TO_PROVIDER, DELIVERED, RETRY_PENDING, FAILED
    provider_message_id VARCHAR(255),
    failure_reason TEXT,
    last_error TEXT,
    submitted_at TIMESTAMP
);

-- 3. University Office Referral Table
CREATE TABLE IF NOT EXISTS university_office_referral (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL REFERENCES issue_report(id) ON DELETE CASCADE,
    referring_leader_id BIGINT NOT NULL REFERENCES community_user(id),
    office_name VARCHAR(255) NOT NULL,
    office_contact VARCHAR(255),
    referral_channel VARCHAR(100), -- EMAIL, PHONE, IN_PERSON, OFFICIAL_LETTER
    referral_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    office_ack_status VARCHAR(50) DEFAULT 'PENDING_ACK', -- PENDING_ACK, ACKNOWLEDGED, REJECTED, ACTION_TAKEN
    office_response TEXT,
    follow_up_date TIMESTAMP,
    outcome TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Alter issue_report to support University linking, Campus name, Routing status, Assigned leader, Urgency
ALTER TABLE issue_report ADD COLUMN IF NOT EXISTS university_id BIGINT REFERENCES university(id);
ALTER TABLE issue_report ADD COLUMN IF NOT EXISTS campus_name VARCHAR(255);
ALTER TABLE issue_report ADD COLUMN IF NOT EXISTS urgency VARCHAR(50) DEFAULT 'NORMAL';
ALTER TABLE issue_report ADD COLUMN IF NOT EXISTS routing_status VARCHAR(50) DEFAULT 'UNASSIGNED'; -- UNASSIGNED, ASSIGNED_TO_LEADER, LEADER_ACKNOWLEDGED, REFERRED_TO_UNIVERSITY, AWAITING_RESPONSE, ROUTING_FAILED, CLOSED
ALTER TABLE issue_report ADD COLUMN IF NOT EXISTS assigned_leader_id BIGINT REFERENCES community_user(id);
ALTER TABLE issue_report ADD COLUMN IF NOT EXISTS notification_status VARCHAR(50) DEFAULT 'PENDING';

-- 5. Indexes for Query Performance
CREATE INDEX IF NOT EXISTS idx_student_leader_univ_status ON student_leader_assignment(university_id, status);
CREATE INDEX IF NOT EXISTS idx_student_leader_user ON student_leader_assignment(leader_id);
CREATE INDEX IF NOT EXISTS idx_email_notification_state_retry ON email_notification(delivery_state, next_retry_at);
CREATE INDEX IF NOT EXISTS idx_email_notification_report ON email_notification(report_id);
CREATE INDEX IF NOT EXISTS idx_office_referral_report ON university_office_referral(report_id);
CREATE INDEX IF NOT EXISTS idx_issue_report_univ_routing ON issue_report(university_id, routing_status);
