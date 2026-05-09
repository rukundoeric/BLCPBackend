CREATE TABLE attachments (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    version        BIGINT       NOT NULL DEFAULT 0,
    state          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    filename       VARCHAR(255) NOT NULL,
    mime_type      VARCHAR(100) NOT NULL,
    file_size      BIGINT       NOT NULL,
    file_path      VARCHAR(500) NOT NULL,
    uploaded_by_id UUID         REFERENCES users(id),
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);
