CREATE TABLE IF NOT EXISTS user_feedback (
    id BIGSERIAL PRIMARY KEY,
    feedback_text TEXT NOT NULL,
    contributor_name VARCHAR(255),
    contributor_contact VARCHAR(255),
    status VARCHAR(50) DEFAULT 'PENDING',
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
