# 🔑 Получение Service Role Key

## Шаг 1: Войдите в Supabase
1. Перейдите на https://supabase.com
2. Войдите в ваш проект: `fqyklholxklhwydksazc`

## Шаг 2: Получите Service Role Key
1. В левом меню нажмите **Settings** (Настройки)
2. Выберите **API**
3. Найдите секцию **Project API keys**
4. Скопируйте **service_role** ключ (НЕ anon key!)

## Шаг 3: Добавьте в .env файл
Добавьте эту строку в файл `NODEJS/.env`:

```
SUPABASE_SERVICE_ROLE_KEY=ваш-service-role-key-здесь
```

## Шаг 4: Создайте таблицу users
В Supabase перейдите в **SQL Editor** и выполните:

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

-- Политики для доступа
CREATE POLICY "Users can read all users" ON users FOR SELECT USING (true);
CREATE POLICY "Users can insert users" ON users FOR INSERT WITH CHECK (true);
CREATE POLICY "Users can update users" ON users FOR UPDATE USING (true);
```

## Шаг 5: Перезапустите сервер
```bash
cd NODEJS
npm run dev
```

После этого activation code будет работать с реальной БД!
