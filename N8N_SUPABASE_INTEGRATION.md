# 🔗 n8n + Google OAuth + Supabase Integration

## 🎯 Цель
Настроить полную интеграцию Google OAuth через n8n с сохранением в Supabase.

---

## 📋 Архитектура

```
User → n8n Webhook → Google OAuth → n8n Callback → Backend → Supabase
                                          ↓
                                   (save tokens)
```

---

## 🔧 Вариант 1: Простой (текущий)

**Что делает:**
- n8n только перенаправляет на Google
- Backend получает код от Google
- Backend сохраняет токен в Supabase (уже реализовано)

**Код для n8n (Code Node):**

```javascript
// Формируем URL для Google OAuth с Google Drive доступом
const clientId = '340752343067-79ipapn7o97qd8ibqvgpjg4687fm7jo7.apps.googleusercontent.com';
const redirectUri = 'http://5.83.140.54:8089/api/auth/google/callback';

// Scopes: базовая инфо + Google Drive readonly
const scopes = [
  'openid',
  'profile', 
  'email',
  'https://www.googleapis.com/auth/drive.readonly'
];

const googleAuthUrl = 
  `https://accounts.google.com/o/oauth2/v2/auth?` +
  `client_id=${encodeURIComponent(clientId)}&` +
  `redirect_uri=${encodeURIComponent(redirectUri)}&` +
  `response_type=code&` +
  `scope=${encodeURIComponent(scopes.join(' '))}&` +
  `access_type=offline&` +
  `prompt=consent`;

console.log('🔐 Google OAuth URL generated with Drive scope');

return [{
  json: {
    redirectUrl: googleAuthUrl
  }
}];
```

---

## 🚀 Вариант 2: Расширенный (n8n обрабатывает callback)

**Что делает:**
- n8n перенаправляет на Google
- Google возвращает callback в n8n
- n8n обменивает код на токен
- n8n сохраняет в Supabase
- n8n перенаправляет на backend с токеном

### Workflow структура:

```
┌─────────────┐
│ Webhook 1   │ GET /webhook/... → Redirect to Google
│ (Start)     │
└─────────────┘

┌─────────────┐
│ Webhook 2   │ GET /callback → Receive code from Google
│ (Callback)  │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ HTTP Request│ Exchange code for token
│  to Google  │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ HTTP Request│ Get user info from Google
│  User Info  │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Supabase   │ Save/update user + tokens
│   Upsert    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Respond    │ Redirect to backend with data
│ to Webhook  │
└─────────────┘
```

### Node 1: Start Webhook (уже есть)

**Settings:**
```
HTTP Method: GET
Path: 07a96af0-9f1f-44e7-bad3-86a2c4e0cb28
```

**Code:** (используй код из Варианта 1 выше)

---

### Node 2: Callback Webhook (новый)

**Нажми "+" → "Webhook"**

**Settings:**
```
HTTP Method: GET
Path: google-oauth-callback
Response Mode: Respond to Webhook
```

---

### Node 3: Exchange Code for Token

**Нажми "+" → "HTTP Request"**

**Settings:**
```
Method: POST
URL: https://oauth2.googleapis.com/token
Authentication: None
Send Body: Yes (Form-Data)
```

**Body Parameters:**
```javascript
code: ={{ $json.query.code }}
client_id: 340752343067-79ipapn7o97qd8ibqvgpjg4687fm7jo7.apps.googleusercontent.com
client_secret: YOUR_GOOGLE_CLIENT_SECRET
redirect_uri: https://guglovskij.app.n8n.cloud/webhook/google-oauth-callback
grant_type: authorization_code
```

⚠️ **ВАЖНО:** Добавь Google Client Secret!

---

### Node 4: Get User Info

**Нажми "+" → "HTTP Request"**

**Settings:**
```
Method: GET
URL: https://www.googleapis.com/oauth2/v3/userinfo
Authentication: Generic Credential Type
```

**Headers:**
```
Authorization: Bearer {{ $json.access_token }}
```

---

### Node 5: Save to Supabase

**Нажми "+" → "HTTP Request"**

**Settings:**
```
Method: POST
URL: https://fqyklholxklhwydksazc.supabase.co/rest/v1/users
Authentication: None
```

**Headers:**
```
apikey: YOUR_SUPABASE_ANON_KEY
Authorization: Bearer YOUR_SUPABASE_ANON_KEY
Content-Type: application/json
Prefer: resolution=merge-duplicates
```

**Body (JSON):**
```javascript
{
  "email": "={{ $json.email }}",
  "full_name": "={{ $json.name }}",
  "google_oauth_token": "={{ $node['Exchange Code'].json.access_token }}",
  "google_refresh_token": "={{ $node['Exchange Code'].json.refresh_token }}",
  "role": "EMPLOYEE",
  "updated_at": "={{ new Date().toISOString() }}"
}
```

---

### Node 6: Respond to Webhook

**Настройки:**
```
Respond With: Redirect
Redirect URL: http://5.83.140.54:8089/api/auth/google/callback?token={{ $node['Exchange Code'].json.access_token }}&email={{ $json.email }}
```

---

## 🔐 Необходимые credentials:

### 1. Google Client Secret

Получи в: https://console.cloud.google.com/apis/credentials

### 2. Supabase Keys

Из твоего проекта:
```
URL: https://fqyklholxklhwydksazc.supabase.co
Anon Key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 📊 Какой вариант выбрать?

### Используй Вариант 1 (Простой) если:
- ✅ Хочешь быстро запустить
- ✅ Backend уже работает с Google OAuth
- ✅ Не нужна сложная логика в n8n

### Используй Вариант 2 (Расширенный) если:
- ✅ Хочешь всю логику OAuth в n8n
- ✅ Нужно сохранять токены сразу в Supabase
- ✅ Backend только для UI

---

## 🎯 Рекомендация: Начни с Варианта 1

**Почему:**
1. Проще настроить (3 ноды вместо 6)
2. Backend уже умеет работать с Google
3. Можно легко расширить потом

**Когда переходить на Вариант 2:**
- Когда нужна более сложная логика
- Когда хочешь отделить OAuth от backend
- Когда нужны дополнительные проверки

---

## ✅ Текущий статус (Вариант 1):

```javascript
// КОД ДЛЯ n8n Code Node (Вариант 1)
const clientId = '340752343067-79ipapn7o97qd8ibqvgpjg4687fm7jo7.apps.googleusercontent.com';
const redirectUri = 'http://5.83.140.54:8089/api/auth/google/callback';

const scopes = [
  'openid',
  'profile', 
  'email',
  'https://www.googleapis.com/auth/drive.readonly'  // ← Google Drive доступ
];

const googleAuthUrl = 
  `https://accounts.google.com/o/oauth2/v2/auth?` +
  `client_id=${encodeURIComponent(clientId)}&` +
  `redirect_uri=${encodeURIComponent(redirectUri)}&` +
  `response_type=code&` +
  `scope=${encodeURIComponent(scopes.join(' '))}&` +
  `access_type=offline&` +
  `prompt=consent`;

return [{
  json: {
    redirectUrl: googleAuthUrl
  }
}];
```

**Этот код уже добавлен в `N8N_SETUP_QUICK.md`!**

---

## 🚀 Следующие шаги:

1. ✅ Используй обновлённый код из `N8N_SETUP_QUICK.md`
2. ✅ Настрой n8n workflow (Вариант 1)
3. ✅ Активируй workflow
4. ✅ Протестируй авторизацию

### Backend уже готов:
- ✅ Сохраняет Google токен в Supabase (`google_oauth_token`)
- ✅ Работает с refresh token
- ✅ Обновляет при повторном входе

---

## 📝 Что добавлено:

### Google Drive Scope
```
https://www.googleapis.com/auth/drive.readonly
```

Теперь приложение может:
- Читать файлы из Google Drive
- Получать метаданные файлов
- Получать список файлов

**НЕ может:**
- ❌ Изменять файлы
- ❌ Удалять файлы
- ❌ Создавать файлы

---

## 🔧 Будущие улучшения для Supabase:

### 1. Row Level Security (RLS)

```sql
-- Политики для таблицы users
CREATE POLICY "Users can view own data"
  ON users FOR SELECT
  USING (auth.uid() = id);

CREATE POLICY "Users can update own data"
  ON users FOR UPDATE
  USING (auth.uid() = id);
```

### 2. Supabase Auth интеграция

```javascript
// В будущем можно использовать Supabase Auth
const { data, error } = await supabase.auth.signInWithOAuth({
  provider: 'google',
  options: {
    scopes: 'openid profile email https://www.googleapis.com/auth/drive.readonly'
  }
})
```

---

## ✅ Итог

**Текущая реализация (Вариант 1):**
- ✅ n8n перенаправляет с Drive scope
- ✅ Backend обрабатывает callback
- ✅ Backend сохраняет в Supabase
- ✅ Всё готово к использованию!

**Используй код из `N8N_SETUP_QUICK.md` и всё заработает!** 🚀

