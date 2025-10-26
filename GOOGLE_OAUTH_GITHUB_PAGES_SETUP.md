# 🚀 Google OAuth через GitHub Pages - Быстрый старт

## 🎯 Почему через GitHub Pages?

**Проблема:** n8n не может делать прямой HTTP 302 редирект  
**Решение:** Использовать промежуточную HTML страницу на GitHub Pages

---

## ⚡ Быстрый старт (15 минут)

### Шаг 1: Создай GitHub репозиторий

1. Открой https://github.com/new
2. Название: `oauth-bridge`
3. Public ✅
4. Create repository

### Шаг 2: Загрузи bridge.html

1. В репозитории нажми **"Add file" → "Upload files"**
2. Загрузи файл `bridge.html` (из этого проекта)
3. Commit changes

### Шаг 3: Включи GitHub Pages

1. Репозиторий → **Settings** → **Pages**
2. Source: **Deploy from a branch**
3. Branch: **main** / (root)
4. Save

**Подожди 1-2 минуты!**

### Шаг 4: Получи URL

Вернись в Settings → Pages, скопируй URL:
```
https://YOUR_GITHUB_USERNAME.github.io/oauth-bridge/
```

Полный URL страницы:
```
https://YOUR_GITHUB_USERNAME.github.io/oauth-bridge/bridge.html
```

---

## 🔧 Настройка Google Cloud Console

### 1. Открой Google Cloud Console
https://console.cloud.google.com/apis/credentials

### 2. Выбери свой проект (или создай новый)

### 3. Создай OAuth 2.0 Client ID

**Credentials → Create Credentials → OAuth client ID**

```
Application type: Web application
Name: Evalyze OAuth

Authorized redirect URIs:
  https://YOUR_GITHUB_USERNAME.github.io/oauth-bridge/bridge.html

Save
```

### 4. Скопируй Client ID и Client Secret

```
Client ID: 340752343067-79ipapn7o97qd8ibqvgpjg4687fm7jo7.apps.googleusercontent.com
Client secret: [скопируй и сохрани безопасно]
```

### 5. Включи Google Drive API

**APIs & Services → Enable APIs and Services → Google Drive API → Enable**

---

## 🛠️ Настройка bridge.html

### Отредактируй файл на GitHub:

1. Открой `bridge.html` в репозитории
2. Нажми кнопку "Edit" (карандаш)
3. Найди секцию `CONFIG` и замени:

```javascript
const CONFIG = {
    CLIENT_ID: '340752343067-79ipapn7o97qd8ibqvgpjg4687fm7jo7.apps.googleusercontent.com', // твой Client ID
    REDIRECT_URI: 'https://YOUR_GITHUB_USERNAME.github.io/oauth-bridge/bridge.html', // ← ЗАМЕНИ!
    N8N_WEBHOOK_URL: 'https://guglovskij.app.n8n.cloud/webhook/google-oauth-callback', // создашь позже
    BACKEND_URL: 'http://5.83.140.54:8089', // твой backend
    SCOPES: [
        'openid',
        'profile',
        'email',
        'https://www.googleapis.com/auth/drive.readonly'
    ]
};
```

4. Commit changes

---

## 🔗 Настройка n8n (2 workflow)

### Workflow #1: "OAuth Start" (редирект на bridge)

#### Node 1: Webhook
```
HTTP Method: GET
Path: 07a96af0-9f1f-44e7-bad3-86a2c4e0cb28
Response Mode: lastNode
```

#### Node 2: Code
```javascript
const BRIDGE_URL = 'https://YOUR_GITHUB_USERNAME.github.io/oauth-bridge/bridge.html';

const html = `<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="refresh" content="0; url=${BRIDGE_URL}">
  <script>window.location.href="${BRIDGE_URL}";</script>
</head>
<body><p>Redirecting...</p></body>
</html>`;

return { response: html };
```

#### Node 3: Respond to Webhook
```
Respond With: Text
Response Body: ={{ $json.response }}

Options → Response Headers:
  Name: Content-Type
  Value: text/html; charset=utf-8
```

**Save & Activate ✅**

---

### Workflow #2: "OAuth Callback" (обработка токенов)

#### Node 1: Webhook (Callback)
```
HTTP Method: POST
Path: google-oauth-callback
Response Mode: responseNode
```

#### Node 2: HTTP Request (Exchange Code)
```
Method: POST
URL: https://oauth2.googleapis.com/token
Body Content Type: Form-Data

Parameters:
  code: ={{ $json.code }}
  client_id: 340752343067-79ipapn7o97qd8ibqvgpjg4687fm7jo7.apps.googleusercontent.com
  client_secret: YOUR_CLIENT_SECRET_HERE
  redirect_uri: ={{ $json.redirect_uri }}
  grant_type: authorization_code
```

#### Node 3: HTTP Request (Get User Info)
```
Method: GET
URL: https://www.googleapis.com/oauth2/v3/userinfo

Headers:
  Authorization: Bearer ={{ $node["HTTP Request"].json.access_token }}
```

#### Node 4: Respond to Webhook
```
Respond With: JSON

Body:
{
  "status": "ok",
  "access_token": "={{ $node["HTTP Request"].json.access_token }}",
  "email": "={{ $node["HTTP Request1"].json.email }}",
  "name": "={{ $node["HTTP Request1"].json.name }}"
}
```

**Save & Activate ✅**

**Скопируй Production URL:** `https://guglovskij.app.n8n.cloud/webhook/google-oauth-callback`

---

## 🔄 Обнови bridge.html с URL n8n Workflow #2

Вернись в GitHub → `bridge.html` → Edit:

```javascript
N8N_WEBHOOK_URL: 'https://guglovskij.app.n8n.cloud/webhook/google-oauth-callback', // ← вставь свой URL
```

Commit changes

---

## ✅ Тестирование

### 1. Проверь GitHub Pages работает:
```
https://YOUR_GITHUB_USERNAME.github.io/oauth-bridge/bridge.html
```
Должен показать "Перенаправление на Google..." и перейти на Google

### 2. Проверь n8n Workflow #1:
```
http://5.83.140.54:8089/api/auth/google/url
```
Должен перенаправить на GitHub Pages

### 3. Полный тест:
```
Сайт → Кнопка "Войти через Google" → Весь flow → Залогинен
```

---

## 🎯 Финальный checklist:

- [ ] GitHub репозиторий создан
- [ ] bridge.html загружен
- [ ] GitHub Pages включён и работает
- [ ] Google Cloud Console настроен (Client ID, Redirect URI, Drive API)
- [ ] n8n Workflow #1 создан и активирован
- [ ] n8n Workflow #2 создан и активирован
- [ ] bridge.html обновлён с правильными URLs (REDIRECT_URI, N8N_WEBHOOK_URL)
- [ ] Backend готов (уже настроен ✅)
- [ ] Тест прошёл успешно

---

## 📦 Файлы в проекте:

```
✅ bridge.html - HTML страница для GitHub Pages
✅ N8N_WORKFLOW_FINAL.md - подробная документация workflow
✅ GOOGLE_OAUTH_GITHUB_PAGES_SETUP.md - этот файл (быстрый старт)
```

---

## 🔧 Troubleshooting:

### bridge.html не открывается
- Подожди 5 минут после включения Pages
- Проверь что репозиторий Public
- Проверь Settings → Pages → включён

### Google OAuth ошибка "redirect_uri_mismatch"
- Проверь что в Google Cloud Console указан ТОЧНЫЙ URL bridge.html
- URL должен включать `/bridge.html` в конце
- Проверь http vs https

### n8n не отвечает
- Проверь что workflow активирован (зелёный toggle)
- Проверь что Production URL правильный
- Посмотри Executions в n8n

---

**Теперь всё готово для промышленной OAuth авторизации! 🚀**

**Следующий шаг:** Создай GitHub репозиторий и загрузи `bridge.html`!

