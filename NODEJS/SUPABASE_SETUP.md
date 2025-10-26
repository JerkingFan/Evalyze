# 🚀 Настройка Supabase для Evalyze

## Шаг 1: Создайте проект в Supabase

1. Перейдите на https://supabase.com
2. Нажмите "Start your project"
3. Войдите через GitHub
4. Создайте новый проект:
   - **Name**: `evalyze-backend`
   - **Database Password**: придумайте сложный пароль
   - **Region**: выберите ближайший к вам

## Шаг 2: Получите ключи

1. В панели Supabase перейдите в **Settings** → **API**
2. Скопируйте:
   - **Project URL** (например: `https://abcdefgh.supabase.co`)
   - **anon public** ключ
   - **service_role** ключ

## Шаг 3: Создайте таблицу users

Выполните этот SQL в **SQL Editor** Supabase:

```sql
-- Создание таблицы users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    activation_code VARCHAR(255) UNIQUE,
    status VARCHAR(50) DEFAULT 'invited',
    telegram_chat_id VARCHAR(255),
    password VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Создание индексов
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_activation_code ON users(activation_code);
CREATE INDEX idx_users_status ON users(status);

-- Включение RLS (Row Level Security)
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Политика для чтения (все могут читать)
CREATE POLICY "Users can read all users" ON users
    FOR SELECT USING (true);

-- Политика для вставки (все могут создавать)
CREATE POLICY "Users can insert users" ON users
    FOR INSERT WITH CHECK (true);

-- Политика для обновления (все могут обновлять)
CREATE POLICY "Users can update users" ON users
    FOR UPDATE USING (true);
```

## Шаг 4: Создайте файл .env

Создайте файл `NODEJS/.env` с вашими данными:

```env
# Server Configuration
PORT=8089
NODE_ENV=development

# Supabase Configuration - ЗАМЕНИТЕ НА ВАШИ ДАННЫЕ
SUPABASE_URL=https://ваш-project-id.supabase.co
SUPABASE_ANON_KEY=ваш-anon-key
SUPABASE_SERVICE_ROLE_KEY=ваш-service-role-key

# JWT Configuration
JWT_SECRET=ваш-длинный-секретный-ключ-для-jwt
JWT_EXPIRES_IN=24h

# n8n Webhook URLs
WEBHOOK_ANALYZE_COMPETENCIES=https://guglovskij.app.n8n.cloud/webhook/0d0a654b-772e-447a-9223-8b443f788175
WEBHOOK_ASSIGN_JOB_ROLE=https://guglovskij.app.n8n.cloud/webhook/113447c6-c39e-410c-ab15-4f5ab7809fd9
WEBHOOK_GENERATE_AI_PROFILE=https://guglovskij.app.n8n.cloud/webhook/bbd2959f-bedc-43fc-a558-69c0fe7b4db
```

## Шаг 5: Перезапустите сервер

```bash
cd NODEJS
npm run dev
```

## Шаг 6: Тестирование

1. Откройте http://localhost:8089/create-employee
2. Создайте сотрудника
3. Скопируйте activation_code
4. Войдите по этому коду на http://localhost:8089/

## 🔧 Проверка подключения

В логах сервера вы должны увидеть:
```
✅ Using real Supabase configuration
🔗 Supabase URL: https://ваш-project-id.supabase.co
```

Вместо:
```
⚠️  Using mock Supabase configuration for testing
```

## 🆘 Если что-то не работает

1. Проверьте, что все ключи скопированы правильно
2. Убедитесь, что таблица `users` создана
3. Проверьте RLS политики
4. Посмотрите логи в терминале сервера
