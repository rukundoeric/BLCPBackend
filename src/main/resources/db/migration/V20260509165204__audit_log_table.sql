CREATE TABLE audit_log (
    id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    version            BIGINT      NOT NULL DEFAULT 0,
    state              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    application_number VARCHAR(20) NOT NULL,
    application_id     UUID        NOT NULL REFERENCES applications(id),
    event              VARCHAR(10) NOT NULL,
    comment            TEXT,
    initial_state      VARCHAR(30) NOT NULL,
    new_state          VARCHAR(30) NOT NULL,
    actor_id           UUID        REFERENCES users(id),
    created_at         TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_id ON audit_log(application_id);
