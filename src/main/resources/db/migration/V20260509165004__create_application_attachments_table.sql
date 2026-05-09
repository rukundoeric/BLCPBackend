CREATE TABLE application_attachments (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    version        BIGINT      NOT NULL DEFAULT 0,
    state          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    application_id UUID        NOT NULL REFERENCES applications(id),
    attachment_id  UUID        NOT NULL REFERENCES attachments(id),
    document_type  VARCHAR(50) NOT NULL,
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_app_attachments_application_id ON application_attachments(application_id);
