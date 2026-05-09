CREATE TABLE application_preferences (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    version        BIGINT      NOT NULL DEFAULT 0,
    state          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    application_id UUID        NOT NULL REFERENCES applications(id),
    preference_key VARCHAR(50) NOT NULL,
    value          TEXT        NOT NULL,
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_app_preference UNIQUE (application_id, preference_key)
);

CREATE INDEX idx_app_preferences_application_id ON application_preferences(application_id);
