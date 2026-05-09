INSERT INTO officers (user_id, role_id, level)
SELECT u.id, r.id, 'LEVEL_1'
FROM users u JOIN roles r ON r.name = 'OFFICER'
WHERE u.email = 'officer@nbr.rw';

INSERT INTO officers (user_id, role_id, level)
SELECT u.id, r.id, 'LEVEL_2'
FROM users u JOIN roles r ON r.name = 'SENIOR_OFFICER'
WHERE u.email = 'senior.officer@nbr.rw';
