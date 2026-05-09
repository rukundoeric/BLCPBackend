INSERT INTO application_preferences (application_id, preference_key, value)
SELECT a.id, 'LEVEL1_OFFICER_ID', u.id::TEXT
FROM applications a, users u
WHERE a.application_number = 'APP-2026-002'
  AND u.email = 'officer@nbr.rw';

INSERT INTO application_preferences (application_id, preference_key, value)
SELECT a.id, 'LEVEL1_OFFICER_COMMENT', 'Application reviewed. Documentation is complete and meets initial requirements.'
FROM applications a
WHERE a.application_number = 'APP-2026-002';
