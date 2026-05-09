CREATE TABLE user_sessions (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    version    BIGINT       NOT NULL DEFAULT 0,
    state      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    user_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP    NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_sessions_token_hash ON user_sessions(token_hash);
CREATE INDEX idx_user_sessions_user_id    ON user_sessions(user_id);
