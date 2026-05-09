INSERT INTO application_state_transitions (application_number, application_id, event, initial_state, new_state, actor_id)
SELECT 'APP-2026-001', a.id, 'APPLY', 'NEW', 'SUBMITTED', NULL
FROM applications a
WHERE a.application_number = 'APP-2026-001';

INSERT INTO application_state_transitions (application_number, application_id, event, initial_state, new_state, actor_id)
SELECT 'APP-2026-002', a.id, 'APPLY', 'NEW', 'SUBMITTED', u.id
FROM applications a, users u
WHERE a.application_number = 'APP-2026-002'
  AND u.email = 'user@gmail.com';

INSERT INTO application_state_transitions (application_number, application_id, event, initial_state, new_state, actor_id)
SELECT 'APP-2026-002', a.id, 'APPROVE', 'SUBMITTED', 'PENDING_FINAL_APPROVAL', u.id
FROM applications a, users u
WHERE a.application_number = 'APP-2026-002'
  AND u.email = 'officer@nbr.rw';
