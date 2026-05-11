CREATE INDEX idx_attachments_uploaded_by_id ON attachments(uploaded_by_id);
CREATE INDEX idx_app_attachments_attachment_id     ON application_attachments(attachment_id);
CREATE INDEX idx_app_attachments_submission_version ON application_attachments(submission_version);
CREATE INDEX idx_applications_applicant_email ON applications(applicant_email);
CREATE INDEX idx_audit_log_application_number ON audit_log(application_number);
CREATE INDEX idx_audit_log_actor_id           ON audit_log(actor_id);
CREATE INDEX idx_audit_log_created_at         ON audit_log(created_at DESC);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
