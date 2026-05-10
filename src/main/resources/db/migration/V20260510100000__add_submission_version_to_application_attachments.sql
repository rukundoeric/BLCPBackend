ALTER TABLE application_attachments
    ADD COLUMN submission_version INT NOT NULL DEFAULT 1;
