# PostgREST (Supabase) - Исправление ошибок 400

## ✅ Что было исправлено

### 🔧 Проблема #1: PostgREST ожидает массив

**Было:**
```json
{ "email": "test@example.com", "role": "EMPLOYEE" }
```

**Стало:**
```json
[{ "email": "test@example.com", "role": "EMPLOYEE" }]
```

PostgREST **всегда ожидает массив** для операций INSERT, даже если вставляется один объект.

### 🔧 Проблема #2: Заголовок Prefer

**Было:**
```
Prefer: return=minimal
```

**Стало:**
```
Prefer: return=representation
```

Теперь PostgREST возвращает вставленный объект, что позволяет получить сгенерированные поля (ID, created_at и т.д.)

### 🔧 Проблема #3: Имена полей (snake_case)

**Правильные имена полей для Supabase:**
```json
{
  "id": "uuid",
  "email": "test@example.com",
  "full_name": "Имя Фамилия",       // НЕ fullName
  "role": "EMPLOYEE",
  "company_id": "uuid",              // НЕ companyId
  "google_oauth_token": "token"     // НЕ googleOauthToken
}
```

## 📝 Обновленные файлы

### 1. SupabaseConfig.java
- Убран дефолтный заголовок `Prefer: return=minimal`
- Каждый запрос теперь может указывать свой Prefer заголовок

### 2. SupabaseUserRepository.java
- `save()` метод теперь:
  - ✅ Отправляет данные как **массив**
  - ✅ Использует **snake_case** для имен полей
  - ✅ Отправляет только `company_id`, а не весь объект Company
  - ✅ Добавляет заголовок `Prefer: return=representation`
  - ✅ Обрабатывает ответ как массив и берет первый элемент

### 3. SupabaseService.java
- `insert()` метод обновлен для работы с массивами
- Автоматически оборачивает объект в массив
- Извлекает первый элемент из массива в ответе

## 🧪 Как протестировать

### 1. Перезапустите приложение:
```bash
pkill -f start.jar
nohup java -jar start.jar > app.log 2>&1 &
```

### 2. Проверьте регистрацию:
```bash
curl -X POST http://5.83.140.54:8089/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "123456",
    "fullName": "Эдвард Эдввардов",
    "role": "EMPLOYEE"
  }'
```

### 3. Проверьте логи:
```bash
tail -f app.log | grep -E "(Inserting|Supabase response|saved successfully)"
```

## 🔍 Отладка

Если все еще получаете 400:

1. **Проверьте RLS политики** в Supabase Dashboard
2. **Проверьте схему таблицы** `users` - какие поля NOT NULL
3. **Проверьте логи Supabase** (Dashboard → Logs)
4. **Попробуйте с SERVICE_ROLE_KEY** (только на сервере!)

## 📊 Структура таблицы users

Убедитесь, что таблица `users` имеет следующую структуру:

```sql
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT UNIQUE NOT NULL,
  full_name TEXT,
  role TEXT NOT NULL,
  company_id UUID REFERENCES companies(id),
  google_oauth_token TEXT,
  created_at TIMESTAMPTZ DEFAULT now()
);
```

## ✅ Ожидаемый результат

После исправлений регистрация должна работать:
```json
{
  "token": "eyJhbGci...",
  "email": "test@example.com",
  "role": "EMPLOYEE",
  "fullName": "Эдвард Эдввардов",
  "companyName": null,
  "message": "Registration successful"
}
```

## 🎯 Важно

1. PostgREST **ВСЕГДА** ожидает массив для INSERT
2. Используйте **snake_case** для имен полей
3. Добавляйте `Prefer: return=representation` для получения ответа
4. Проверьте RLS политики - они могут блокировать INSERT

