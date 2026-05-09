INSERT INTO applications (
    application_number,
    applicant_id,
    applicant_email,
    applicant_first_name,
    applicant_last_name,
    bank_name,
    bank_type,
    notes,
    status,
    processing_level
) VALUES (
    'APP-2026-001',
    NULL,
    'john.doe@example.com',
    'John',
    'Doe',
    'Rwanda First Commercial Bank',
    'COMMERCIAL',
    'Application for a new commercial banking license to operate in Kigali.',
    'SUBMITTED',
    'LEVEL_1'
);

INSERT INTO applications (
    application_number,
    applicant_id,
    applicant_email,
    applicant_first_name,
    applicant_last_name,
    bank_name,
    bank_type,
    notes,
    status,
    processing_level
)
SELECT
    'APP-2026-002',
    u.id,
    u.email,
    u.first_name,
    u.last_name,
    'Alpine Microfinance Institution',
    'MICROFINANCE',
    'Seeking a microfinance license to serve rural communities in the Western Province.',
    'PENDING_FINAL_APPROVAL',
    'LEVEL_2'
FROM users u
WHERE u.email = 'user@gmail.com';
