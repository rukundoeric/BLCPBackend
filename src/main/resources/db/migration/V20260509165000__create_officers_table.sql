CREATE TABLE officers (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    version    BIGINT      NOT NULL DEFAULT 0,
    state      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    user_id    UUID        NOT NULL REFERENCES users(id),
    role_id    UUID        NOT NULL REFERENCES roles(id),
    level      VARCHAR(10) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_officer_user_role UNIQUE (user_id, role_id)
);

CREATE INDEX idx_officers_user_id  ON officers(user_id);
CREATE INDEX idx_officers_level    ON officers(level);
