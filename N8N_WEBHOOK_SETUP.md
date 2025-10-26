# ⚡ Быстрая настройка n8n Webhook для Google OAuth

## 🎯 Цель
Настроить n8n workflow для обработки Google OAuth авторизации и возврата данных пользователя на backend.

---

## 📋 Шаги настройки n8n

### 1. Создайте новый Workflow в n8n

### 2. Добавьте узлы (Nodes)

#### Node 1: Webhook Trigger
- **Тип:** Webhook
- **URL Path:** `/webhook/07a96af0-9f1f-44e7-bad3-86a2c4e0cb28`
- **HTTP Method:** GET и POST (оба)
- **Response Mode:** Respond to Webhook

#### Node 2: Google OAuth2 API
- **Тип:** Google API (или HTTP Request)
- **Action:** Get User Info
- **Credential:** Google OAuth2 API credentials

**Альтернатива:** Если webhook получает уже готовый токен:
```javascript
// Function Node
const token = $input.item.json.token;

// Запрос к Google API
const options = {
  url: 'https://www.googleapis.com/oauth2/v3/userinfo',
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
};

return options;
```

#### Node 3: Format Response
- **Тип:** Function или Set node
```javascript
// Function Node
const userData = $input.item.json;

return [{
  json: {
    email: userData.email,
    name: userData.name || userData.given_name + ' ' + userData.family_name,
    picture: userData.picture
  }
}];
```

#### Node 4: Respond to Webhook
**Вариант A: JSON Response (для POST метода)**
```json
{
  "email": "{{$json.email}}",
  "name": "{{$json.name}}",
  "picture": "{{$json.picture}}"
}
```

**Вариант B: Redirect (для GET метода - основной вариант)**
- **Response Type:** Redirect
- **URL:** `http://5.83.140.54:8089/api/auth/google/callback?token={{$json.token}}&email={{$json.email}}&name={{$json.name}}`

---

## 🔄 Схема Workflow

```
┌─────────────────┐
│ Webhook Trigger │
│   (GET/POST)    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Google OAuth   │
│  Authenticate   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Get User Info   │
│  from Google    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Format Response │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Respond/Redirect│
└─────────────────┘
```

---

## 🛠️ Конфигурация Google OAuth в n8n

### Создайте Google OAuth2 Credentials в n8n:

1. Перейдите в **Credentials** → **New**
2. Выберите **Google OAuth2 API**
3. Введите данные:
   - **Client ID:** `340752343067-79ipapn7o97qd8ibqvgpjg4687fm7jo7.apps.googleusercontent.com`
   - **Client Secret:** (ваш secret от Google)
   - **Scope:** 
     ```
     openid
     profile
     email
     ```
4. **Redirect URI:** Используйте URL из n8n
5. Сохраните credentials

---

## 📡 Варианты использования

### Вариант 1: Полный OAuth Flow в n8n (Рекомендуется)

**Когда:** Пользователь ещё не авторизован в Google

**Flow:**
```
1. Frontend → redirect на n8n webhook
2. n8n → redirect на Google OAuth
3. Google → callback на n8n
4. n8n → получает токен и user info
5. n8n → redirect на backend callback с токеном
```

**Webhook Response:**
- Type: Redirect
- URL: `http://5.83.140.54:8089/api/auth/google/callback?token={{google_token}}`

### Вариант 2: Валидация существующего токена

**Когда:** Пользователь уже получил токен от Google

**Flow:**
```
1. Frontend → POST /api/auth/google/authenticate {token: "..."}
2. Backend → POST к n8n webhook {token: "..."}
3. n8n → валидирует токен в Google
4. n8n → возвращает user info
5. Backend → создаёт JWT
```

**Webhook Response:**
```json
{
  "email": "user@example.com",
  "name": "John Doe",
  "picture": "https://..."
}
```

---

## ✅ Тестирование

### 1. Тест Webhook URL
```bash
curl https://guglovskij.app.n8n.cloud/webhook/07a96af0-9f1f-44e7-bad3-86a2c4e0cb28
```
Должно вернуть ответ от n8n (не 404)

### 2. Тест с тестовым токеном
```bash
curl -X POST https://guglovskij.app.n8n.cloud/webhook/07a96af0-9f1f-44e7-bad3-86a2c4e0cb28 \
  -H "Content-Type: application/json" \
  -d '{"token":"test_token"}'
```

### 3. Полный тест через браузер
1. Откройте: `http://5.83.140.54:8089`
2. Нажмите: "Войти через Google"
3. Должен быть редирект на n8n webhook
4. n8n должен редиректнуть на Google
5. После авторизации - возврат на backend

---

## 🔒 Безопасность

### ⚠️ Важно!

1. **Валидируйте токены в n8n:**
   - Проверяйте что токен действительно от Google
   - Используйте официальные Google API endpoints

2. **HTTPS обязателен:**
   - Используйте HTTPS для n8n webhook
   - Используйте HTTPS для backend (или настройте правильно redirect)

3. **Не передавайте client_secret на frontend:**
   - Всё OAuth должно быть в n8n или backend
   - Frontend получает только результат

4. **Токены должны быть временными:**
   - Google access tokens истекают
   - Backend JWT токены должны иметь expiration

---

## 🐛 Отладка

### Проверка n8n workflow:
1. Откройте workflow в n8n
2. Включите **Debug Mode**
3. Отправьте тестовый запрос
4. Смотрите данные на каждом узле

### Проверка backend:
```bash
# Смотрите логи backend
tail -f logs/application.log

# Или в консоли где запущен Spring Boot
# Должны быть сообщения:
# === Google OAuth via n8n START ===
# n8n webhook response: {...}
```

### Типичные ошибки:

**"Failed to get user info from n8n webhook"**
→ n8n не возвращает нужные данные или не доступен

**"Email not received from n8n webhook"**
→ В ответе от n8n нет поля "email"

**"CORS error"**
→ Настройте CORS в n8n webhook settings

---

## 📖 Пример n8n Workflow (JSON)

```json
{
  "name": "Google OAuth Handler",
  "nodes": [
    {
      "name": "Webhook",
      "type": "n8n-nodes-base.webhook",
      "parameters": {
        "path": "07a96af0-9f1f-44e7-bad3-86a2c4e0cb28",
        "httpMethod": "POST",
        "responseMode": "responseNode"
      }
    },
    {
      "name": "Google",
      "type": "n8n-nodes-base.google",
      "parameters": {
        "operation": "getUserInfo",
        "token": "={{$json.token}}"
      },
      "credentials": {
        "googleOAuth2Api": "Google OAuth2"
      }
    },
    {
      "name": "Respond",
      "type": "n8n-nodes-base.respondToWebhook",
      "parameters": {
        "responseBody": "={{$json}}"
      }
    }
  ],
  "connections": {
    "Webhook": {
      "main": [[{"node": "Google"}]]
    },
    "Google": {
      "main": [[{"node": "Respond"}]]
    }
  }
}
```

Вы можете импортировать этот JSON в n8n как отправную точку.

---

## ✨ Готово!

После настройки n8n workflow авторизация через Google будет работать автоматически!

**Вопросы?** Проверьте:
1. ✅ Логи n8n workflow
2. ✅ Логи Spring Boot backend
3. ✅ Network tab в браузере
4. ✅ Документацию: `GOOGLE_OAUTH_N8N.md`

