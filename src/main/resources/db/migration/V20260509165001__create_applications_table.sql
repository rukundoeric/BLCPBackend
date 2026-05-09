CREATE TABLE applications (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    version              BIGINT       NOT NULL DEFAULT 0,
    state                VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    application_number   VARCHAR(20)  NOT NULL UNIQUE,
    applicant_id         UUID         REFERENCES users(id),
    applicant_email      VARCHAR(255) NOT NULL,
    applicant_first_name VARCHAR(100) NOT NULL,
    applicant_last_name  VARCHAR(100) NOT NULL,
    bank_name            VARCHAR(200) NOT NULL,
    bank_type            VARCHAR(100) NOT NULL,
    notes                TEXT,
    status               VARCHAR(30)  NOT NULL DEFAULT 'NEW',
    processing_level     VARCHAR(10)  NOT NULL DEFAULT 'LEVEL_1',
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_applications_applicant_id    ON applications(applicant_id);
CREATE INDEX idx_applications_status          ON applications(status);
CREATE INDEX idx_applications_processing_level ON applications(processing_level);
