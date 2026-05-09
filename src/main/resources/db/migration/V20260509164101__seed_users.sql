INSERT INTO users (email, password, first_name, last_name) VALUES
    ('user@gmail.com',      '$2b$10$yh9QgcqNGbC8yhtuAjMCUOBxPxbmtXjDMkt0hHqhssuHxHFaa46lK', 'Alice',  'Mugisha'),
    ('officer@nbr.rw',        '$2b$10$Ppcsuv4VQD5u1YLAXH/ruOs0Zy002r4PVFTuILSH0wQaP6TID2KRq', 'Bob',    'Nkurunziza'),
    ('senior.officer@nbr.rw', '$2b$10$.tnDTZAFiHxUlHvNy4Rxhu6Flf7ErJtbYteoXeXsUkUSPapWIzoCC', 'Claire', 'Uwimana'),
    ('admin@nbr.rw',          '$2b$10$hfaPAiw7gBsi7k.MSsykk.Vgi/7HnVVbd/clD8dKoGypF3ktl2oam', 'David',  'Habimana');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'APPLICANT'    WHERE u.email = 'user@gmail.com';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'OFFICER'       WHERE u.email = 'officer@nbr.rw';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'SENIOR_OFFICER' WHERE u.email = 'senior.officer@nbr.rw';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'ADMIN'          WHERE u.email = 'admin@nbr.rw';
