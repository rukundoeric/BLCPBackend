INSERT INTO application_attachments (application_id, attachment_id, document_type)
SELECT a.id, att.id, 'BUSINESS_PLAN'
FROM applications a JOIN attachments att ON att.filename = 'business_plan.pdf'
WHERE a.application_number = 'APP-2026-001';

INSERT INTO application_attachments (application_id, attachment_id, document_type)
SELECT a.id, att.id, 'INCORPORATION_CERTIFICATE'
FROM applications a JOIN attachments att ON att.filename = 'incorporation_cert.pdf'
WHERE a.application_number = 'APP-2026-001';

INSERT INTO application_attachments (application_id, attachment_id, document_type)
SELECT a.id, att.id, 'FINANCIAL_STATEMENTS'
FROM applications a JOIN attachments att ON att.filename = 'financial_statements.pdf'
WHERE a.application_number = 'APP-2026-002';

INSERT INTO application_attachments (application_id, attachment_id, document_type)
SELECT a.id, att.id, 'OWNERSHIP_STRUCTURE'
FROM applications a JOIN attachments att ON att.filename = 'ownership_structure.pdf'
WHERE a.application_number = 'APP-2026-002';
