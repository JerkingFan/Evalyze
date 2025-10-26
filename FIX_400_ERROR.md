# 🔥 ИСПРАВЛЕНИЕ ОШИБКИ 400 (Registration Failed)

## ⚠️ Причина ошибки

**99% вероятности** - проблема в **RLS (Row Level Security)** политиках Supabase!

Вы видите в Table Editor: `users` - **"Unrestricted"** - это значит, что RLS **ВКЛЮЧЕНА**, но политики **НЕ НАСТРОЕНЫ** → Supabase блокирует все INSERT операции!

## ✅ РЕШЕНИЕ 1: Быстрое (отключить RLS)

### Шаг 1: Откройте SQL Editor в Supabase Dashboard

1. Перейдите в **Supabase Dashboard**: https://supabase.com/dashboard
2. Выберите ваш проект: `fqyklholxklhwydksazc`
3. Откройте: **SQL Editor** (слева в меню)

### Шаг 2: Выполните этот SQL:

```sql
-- Отключить RLS на таблицах users и companies
ALTER TABLE public.users DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.companies DISABLE ROW LEVEL SECURITY;

-- Проверка (должно быть false)
SELECT tablename, rowsecurity 
FROM pg_tables 
WHERE schemaname = 'public' 
  AND tablename IN ('users', 'companies');
```

### Шаг 3: Нажмите **RUN**

Результат должен быть:
```
tablename  | rowsecurity
-----------+------------
users      | false
companies  | false
```

## ✅ РЕШЕНИЕ 2: Правильное (настроить RLS политики)

Если хотите оставить RLS для безопасности:

```sql
-- 1. Включить RLS
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.companies ENABLE ROW LEVEL SECURITY;

-- 2. Разрешить INSERT для анонимных пользователей (для регистрации)
CREATE POLICY "Allow anonymous insert on users" 
ON public.users 
FOR INSERT 
TO anon 
WITH CHECK (true);

CREATE POLICY "Allow anonymous insert on companies" 
ON public.companies 
FOR INSERT 
TO anon 
WITH CHECK (true);

-- 3. Разрешить SELECT для всех (или только для authenticated)
CREATE POLICY "Allow select on users" 
ON public.users 
FOR SELECT 
TO anon, authenticated
USING (true);

CREATE POLICY "Allow select on companies" 
ON public.companies 
FOR SELECT 
TO anon, authenticated
USING (true);
```

## 🔍 ОТЛАДКА (если не помогло)

### Шаг 1: Пересоберите приложение

```bash
./gradlew clean build -x test
```

### Шаг 2: Перезапустите на сервере

```bash
# SSH на сервер
ssh root@5.83.140.54

# Остановить старое приложение
pkill -f start.jar

# Загрузить новый JAR (с вашего компьютера)
exit
scp build/libs/NEW_NEW_MVP-0.0.1-SNAPSHOT.jar root@5.83.140.54:~/start.jar

# Снова на сервер
ssh root@5.83.140.54

# Запустить
nohup java -jar start.jar > app.log 2>&1 &

# Смотреть логи
tail -f app.log
```

### Шаг 3: Попробуйте зарегистрироваться

Теперь в логах вы увидите:

**При успехе:**
```
=== SUPABASE INSERT DEBUG ===
Inserting user into Supabase: {id=..., email=test@example.com, full_name=..., role=EMPLOYEE}
Request body (as array): [{...}]
=== SUCCESS ===
User saved successfully: test@example.com
===============
```

**При ошибке RLS:**
```
=== SUPABASE ERROR RESPONSE ===
Status: 403 Forbidden
Body: {"message":"new row violates row-level security policy for table \"users\""}
================================
```

**При ошибке полей:**
```
=== SUPABASE ERROR RESPONSE ===
Status: 400 Bad Request
Body: {"message":"null value in column \"...\" violates not-null constraint"}
================================
```

## 🎯 ПРОВЕРКА СТРУКТУРЫ ТАБЛИЦЫ

Убедитесь, что таблица `users` имеет правильную структуру:

```sql
-- Посмотреть структуру таблицы
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default
FROM information_schema.columns
WHERE table_schema = 'public' 
  AND table_name = 'users'
ORDER BY ordinal_position;
```

**Ожидаемые колонки:**
- `id` - UUID, NOT NULL, DEFAULT gen_random_uuid()
- `email` - TEXT, NOT NULL, UNIQUE
- `full_name` - TEXT, NULL
- `role` - TEXT, NOT NULL
- `company_id` - UUID, NULL (FK to companies)
- `google_oauth_token` - TEXT, NULL
- `created_at` - TIMESTAMPTZ, DEFAULT now()

## 📝 ЧЕКЛИСТ

- [ ] Выполнил SQL для отключения RLS (или настройки политик)
- [ ] Пересобрал приложение (`./gradlew clean build -x test`)
- [ ] Загрузил новый JAR на сервер
- [ ] Перезапустил приложение
- [ ] Попробовал зарегистрироваться
- [ ] Проверил логи (`tail -f app.log`)

## 🆘 ЕСЛИ ВСЁ ЕЩЁ НЕ РАБОТАЕТ

Пришлите мне:
1. **Полные логи** из `app.log` (раздел `=== SUPABASE ERROR RESPONSE ===`)
2. **Результат SQL проверки** структуры таблицы
3. **Скриншот** RLS настроек из Supabase Dashboard (Table Editor → users → RLS)

