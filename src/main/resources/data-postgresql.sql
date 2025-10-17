-- Evalyze PostgreSQL Test Data

-- Insert sample companies
INSERT INTO companies (name)
VALUES
  ('Компания А'),
  ('Компания Б'),
  ('Компания В');

-- Insert sample users
INSERT INTO users (
  company_id,
  email,
  full_name,
  role,
  google_oauth_token
)
VALUES
  (
    (SELECT id FROM companies WHERE name = 'Компания А'),
    'user1@company_a.com',
    'Иван Иванов',
    'EMPLOYEE',
    json_build_object('token', 'example_token_1')
  ),
  (
    (SELECT id FROM companies WHERE name = 'Компания А'),
    'user2@company_a.com',
    'Петр Петров',
    'ADMIN',
    json_build_object('token', 'example_token_2')
  ),
  (
    (SELECT id FROM companies WHERE name = 'Компания Б'),
    'user3@company_b.com',
    'Светлана Светлова',
    'EMPLOYEE',
    json_build_object('token', 'example_token_3')
  );

-- Insert sample invitations
INSERT INTO invitations (
  company_id,
  email,
  invitation_code,
  status,
  expires_at
)
VALUES
  (
    (SELECT id FROM companies WHERE name = 'Компания А'),
    'invitee1@company_a.com',
    'INVITE123',
    'PENDING',
    NOW() + INTERVAL '7 days'
  ),
  (
    (SELECT id FROM companies WHERE name = 'Компания Б'),
    'invitee2@company_b.com',
    'INVITE456',
    'PENDING',
    NOW() + INTERVAL '7 days'
  );

-- Insert sample profiles
INSERT INTO profiles (user_id, profile_data, status)
VALUES
  (
    (SELECT id FROM users WHERE email = 'user1@company_a.com'),
    json_build_object('bio', 'Разработчик', 'skills', json_build_array('Python', 'Django')),
    'PENDING'
  ),
  (
    (SELECT id FROM users WHERE email = 'user2@company_a.com'),
    json_build_object('bio', 'Менеджер', 'skills', json_build_array('Управление проектами')),
    'COMPLETED'
  );

-- Insert sample profile snapshots
INSERT INTO profile_snapshots (user_id, snapshot_date, profile_data)
VALUES
  (
    (SELECT id FROM users WHERE email = 'user1@company_a.com'),
    NOW(),
    json_build_object('bio', 'Разработчик', 'skills', json_build_array('Python', 'Django'))
  ),
  (
    (SELECT id FROM users WHERE email = 'user2@company_a.com'),
    NOW(),
    json_build_object('bio', 'Менеджер', 'skills', json_build_array('Управление проектами'))
  );

-- Insert sample company content
INSERT INTO company_content (
  company_id,
  content_type,
  title,
  data
)
VALUES
  (
    (SELECT id FROM companies WHERE name = 'Компания А'),
    'JOB_ROLE',
    'Senior Python Developer',
    json_build_object('salary', '100000', 'location', 'Москва')
  ),
  (
    (SELECT id FROM companies WHERE name = 'Компания Б'),
    'COURSE',
    'Курс по Docker',
    json_build_object('duration', '4 weeks', 'price', '20000')
  );
