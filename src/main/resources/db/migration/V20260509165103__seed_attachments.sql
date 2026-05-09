INSERT INTO attachments (filename, mime_type, file_size, file_path, uploaded_by_id)
VALUES
    ('business_plan.pdf',        'application/pdf', 2048000, 'business_plan.pdf',        NULL),
    ('incorporation_cert.pdf',   'application/pdf',  512000, 'incorporation_cert.pdf',   NULL);

INSERT INTO attachments (filename, mime_type, file_size, file_path, uploaded_by_id)
SELECT 'financial_statements.pdf', 'application/pdf', 3145728, 'financial_statements.pdf', u.id
FROM users u WHERE u.email = 'user@gmail.com';

INSERT INTO attachments (filename, mime_type, file_size, file_path, uploaded_by_id)
SELECT 'ownership_structure.pdf',  'application/pdf', 1024000, 'wnership_structure.pdf',  u.id
FROM users u WHERE u.email = 'user@gmail.com';
