# 🎯 n8n Workflow для Google OAuth (Финальная версия)

## 📋 Два workflow нужны:

### 1. Workflow "OAuth Start" (простой редирект)
### 2. Workflow "OAuth Callback" (обработка токенов)

---

## 🔧 Workflow #1: OAuth Start

**Назначение:** Перенаправляет пользователя на bridge page

### Node 1: Webhook (Start)
```
HTTP Method: GET
Path: 07a96af0-9f1f-44e7-bad3-86a2c4e0cb28
Response Mode: lastNode
```

### Node 2: Code
```javascript
// URL твоей bridge page на GitHub Pages
const BRIDGE_URL = 'https://YOUR_GITHUB_USERNAME.github.io/oauth-bridge/bridge.html';

const html = `<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="refresh" content="0; url=${BRIDGE_URL}">
  <script>window.location.href="${BRIDGE_URL}";</script>
</head>
<body>
  <p>Redirecting...</p>
</body>
</html>`;

return { response: html };
```

### Node 3: Respond to Webhook
```
Respond With: Text
Response Body: ={{ $json.response }}

Options → Response Headers:
  Name: Content-Type
  Value: text/html; charset=utf-8
```

---

## 🔧 Workflow #2: OAuth Callback

**Назначение:** Обменивает code на token и сохраняет в Supabase

### Node 1: Webhook (Callback)
```
HTTP Method: POST
Path: google-oauth-callback
Response Mode: responseNode
```

### Node 2: Code (Validate)
```javascript
// Проверяем что пришли нужные данные
const { code, state, redirect_uri } = $input.item.json;

if (!code) {
  throw new Error('Missing authorization code');
}

console.log('Received code from Google');
console.log('State:', state);
console.log('Redirect URI:', redirect_uri);

return {
  code,
  state,
  redirect_uri: redirect_uri || 'https://YOUR_GITHUB_USERNAME.github.io/oauth-bridge/bridge.html'
};
```

### Node 3: HTTP Request (Exchange Code for Token)
```
Method: POST
URL: https://oauth2.googleapis.com/token
Authentication: None
Send Body: Yes
Body Content Type: Form-Data

Body Parameters:
  code: ={{ $json.code }}
  client_id: 340752343067-79ipapn7o97qd8ibqvgpjg4687fm7jo7.apps.googleusercontent.com
  client_secret: YOUR_GOOGLE_CLIENT_SECRET
  redirect_uri: ={{ $json.redirect_uri }}
  grant_type: authorization_code
```

### Node 4: HTTP Request (Get User Info)
```
Method: GET
URL: https://www.googleapis.com/oauth2/v3/userinfo
Authentication: Generic Credential Type

Generic Auth Type: Header Auth
Name: Authorization
Value: Bearer ={{ $node["HTTP Request"].json.access_token }}
```

### Node 5: Code (Prepare Supabase Data)
```javascript
// Готовим данные для Supabase
const tokenData = $node["HTTP Request"].json;
const userInfo = $node["HTTP Request1"].json;

return {
  email: userInfo.email,
  full_name: userInfo.name,
  google_oauth_token: tokenData.access_token,
  google_refresh_token: tokenData.refresh_token,
  role: 'EMPLOYEE',
  updated_at: new Date().toISOString()
};
```

### Node 6: HTTP Request (Supabase Upsert)
```
Method: POST
URL: https://fqyklholxklhwydksazc.supabase.co/rest/v1/users
Authentication: None

Headers:
  apikey: YOUR_SUPABASE_ANON_KEY
  Authorization: Bearer YOUR_SUPABASE_ANON_KEY
  Content-Type: application/json
  Prefer: resolution=merge-duplicates

Body Content Type: JSON
Body: ={{ $json }}
```

### Node 7: Respond to Webhook
```
Respond With: JSON

JSON:
{
  "status": "ok",
  "access_token": "={{ $node["HTTP Request"].json.access_token }}",
  "email": "={{ $node["HTTP Request1"].json.email }}",
  "name": "={{ $node["HTTP Request1"].json.name }}"
}
```

---

## 📊 Схема потока:

```
User clicks "Login"
    ↓
Frontend → n8n Workflow #1
    ↓
Redirect to GitHub Pages (bridge.html)
    ↓
Redirect to Google OAuth
    ↓
User authorizes
    ↓
Google → GitHub Pages (bridge.html?code=...)
    ↓
bridge.html → n8n Workflow #2 (POST /google-oauth-callback)
    ↓
n8n exchanges code for token
    ↓
n8n gets user info from Google
    ↓
n8n saves to Supabase
    ↓
n8n responds to bridge.html
    ↓
bridge.html → Backend (/api/auth/google/callback?token=...)
    ↓
Backend generates JWT
    ↓
User logged in ✅
```

---

## 🔐 Что нужно настроить:

### 1. Google Cloud Console
```
Authorized redirect URIs:
  https://YOUR_GITHUB_USERNAME.github.io/oauth-bridge/bridge.html

Enabled APIs:
  - Google+ API
  - Google Drive API
```

### 2. GitHub Pages
```
1. Создай репозиторий: oauth-bridge
2. Загрузи bridge.html
3. Settings → Pages → Enable
4. Получи URL: https://YOUR_GITHUB_USERNAME.github.io/oauth-bridge/bridge.html
```

### 3. bridge.html (обнови конфиг)
```javascript
const CONFIG = {
    CLIENT_ID: '340752343067-79ipapn7o97qd8ibqvgpjg4687fm7jo7.apps.googleusercontent.com',
    REDIRECT_URI: 'https://YOUR_GITHUB_USERNAME.github.io/oauth-bridge/bridge.html',
    N8N_WEBHOOK_URL: 'https://guglovskij.app.n8n.cloud/webhook/google-oauth-callback',
    BACKEND_URL: 'http://5.83.140.54:8089',
    ...
};
```

### 4. n8n workflows
```
Workflow #1: https://guglovskij.app.n8n.cloud/webhook/07a96af0-9f1f-44e7-bad3-86a2c4e0cb28
Workflow #2: https://guglovskij.app.n8n.cloud/webhook/google-oauth-callback
```

### 5. Frontend (app.js)
```javascript
async loginWithGoogle() {
    // Перенаправляем на n8n, который перенаправит на bridge page
    window.location.href = '/api/auth/google/url';
}
```

---

## ✅ Checklist:

- [ ] Google Cloud Console настроен
- [ ] GitHub Pages создан и работает
- [ ] bridge.html загружен на GitHub Pages
- [ ] n8n Workflow #1 создан и активирован
- [ ] n8n Workflow #2 создан и активирован
- [ ] bridge.html обновлён с правильными URLs
- [ ] Frontend обновлён
- [ ] Backend готов принимать callback
- [ ] Supabase RLS настроен
- [ ] Тест OAuth flow прошёл успешно

---

## 🧪 Тестирование:

### 1. Тест redirect на bridge:
```
http://5.83.140.54:8089/api/auth/google/url
→ должен перенаправить на GitHub Pages
```

### 2. Тест bridge page:
```
https://YOUR_GITHUB_USERNAME.github.io/oauth-bridge/bridge.html
→ должен перенаправить на Google OAuth
```

### 3. Тест полного flow:
```
Сайт → Login → GitHub Pages → Google → Authorize → GitHub Pages → n8n → Backend → Logged in
```

---

**Это промышленный стандарт OAuth flow! 🚀**

