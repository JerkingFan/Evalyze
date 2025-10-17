-- Evalyze Database Initialization Script

-- Insert sample companies
INSERT INTO companies (id, name, created_at) VALUES
  (UUID(), 'Компания А', NOW()),
  (UUID(), 'Компания Б', NOW()),
  (UUID(), 'Компания В', NOW());

-- Insert sample users
INSERT INTO users (id, company_id, email, full_name, role, google_oauth_token) VALUES
  (
    UUID(),
    (SELECT id FROM companies WHERE name = 'Компания А' LIMIT 1),
    'user1@company_a.com',
    'Иван Иванов',
    'EMPLOYEE',
    JSON_OBJECT('token', 'example_token_1')
  ),
  (
    UUID(),
    (SELECT id FROM companies WHERE name = 'Компания А' LIMIT 1),
    'user2@company_a.com',
    'Петр Петров',
    'ADMIN',
    JSON_OBJECT('token', 'example_token_2')
  ),
  (
    UUID(),
    (SELECT id FROM companies WHERE name = 'Компания Б' LIMIT 1),
    'user3@company_b.com',
    'Светлана Светлова',
    'EMPLOYEE',
    JSON_OBJECT('token', 'example_token_3')
  );

-- Insert sample invitations
INSERT INTO invitations (id, company_id, email, invitation_code, status, expires_at) VALUES
  (
    UUID(),
    (SELECT id FROM companies WHERE name = 'Компания А' LIMIT 1),
    'invitee1@company_a.com',
    'INVITE123',
    'PENDING',
    NOW() + INTERVAL 7 DAY
  ),
  (
    UUID(),
    (SELECT id FROM companies WHERE name = 'Компания Б' LIMIT 1),
    'invitee2@company_b.com',
    'INVITE456',
    'PENDING',
    NOW() + INTERVAL 7 DAY
  );

-- Insert sample profiles
INSERT INTO profiles (user_id, profile_data, status, last_updated) VALUES
  (
    (SELECT id FROM users WHERE email = 'user1@company_a.com' LIMIT 1),
    JSON_OBJECT('bio', 'Разработчик', 'skills', JSON_ARRAY('Python', 'Django')),
    'PENDING',
    NOW()
  ),
  (
    (SELECT id FROM users WHERE email = 'user2@company_a.com' LIMIT 1),
    JSON_OBJECT('bio', 'Менеджер', 'skills', JSON_ARRAY('Управление проектами')),
    'COMPLETED',
    NOW()
  );

-- Insert sample profile snapshots
INSERT INTO profile_snapshots (id, user_id, snapshot_date, profile_data) VALUES
  (
    UUID(),
    (SELECT id FROM users WHERE email = 'user1@company_a.com' LIMIT 1),
    NOW(),
    JSON_OBJECT('bio', 'Разработчик', 'skills', JSON_ARRAY('Python', 'Django'))
  ),
  (
    UUID(),
    (SELECT id FROM users WHERE email = 'user2@company_a.com' LIMIT 1),
    NOW(),
    JSON_OBJECT('bio', 'Менеджер', 'skills', JSON_ARRAY('Управление проектами'))
  );

-- Insert sample company content
INSERT INTO company_content (id, company_id, content_type, title, data) VALUES
  (
    UUID(),
    (SELECT id FROM companies WHERE name = 'Компания А' LIMIT 1),
    'JOB_ROLE',
    'Senior Python Developer',
    JSON_OBJECT('salary', '100000', 'location', 'Москва')
  ),
  (
    UUID(),
    (SELECT id FROM companies WHERE name = 'Компания Б' LIMIT 1),
    'COURSE',
    'Курс по Docker',
    JSON_OBJECT('duration', '4 weeks', 'price', '20000')
  );
