# ⚡ Быстрая настройка n8n webhook для Google OAuth

## 🎯 Цель
Настроить n8n workflow для редиректа пользователей на Google OAuth.

---

## 📋 Пошаговая инструкция (5 минут)

### Шаг 1: Создай Workflow в n8n

1. Открой: https://guglovskij.app.n8n.cloud/
2. Нажми: **"+ New Workflow"**
3. Название: `Google OAuth Handler`

---

### Шаг 2: Добавь Webhook Node

**Нажми "+" → найди "Webhook"**

Настройки:
```
HTTP Method: GET
Path: 07a96af0-9f1f-44e7-bad3-86a2c4e0cb28
Response Mode: Respond to Webhook
Authentication: None
```

⚠️ **ВАЖНО:** Path БЕЗ слеша в начале!

---

### Шаг 3: Добавь Code Node

**Нажми "+" → найди "Code" → соедини с Webhook**

Вставь этот JavaScript код:

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
  `prompt=consent&` +
  // Добавляем state для Supabase интеграции (опционально)
  `state=${encodeURIComponent(JSON.stringify({source: 'n8n', timestamp: Date.now()}))}`;

console.log('🔐 Google OAuth URL generated with Drive scope');
console.log('Scopes:', scopes.join(', '));

return [{
  json: {
    redirectUrl: googleAuthUrl,
    scopes: scopes,
    clientId: clientId
  }
}];
```

---

### Шаг 4: Добавь Respond to Webhook Node

**Нажми "+" → найди "Respond to Webhook" → соедини с Code**

Настройки:
```
Respond With: Redirect
Redirect URL: ={{ $json.redirectUrl }}
```

⚠️ **ВАЖНО:** Используй синтаксис `={{ }}` для переменных!

---

### Шаг 5: АКТИВИРУЙ Workflow

1. **Нажми Ctrl+S** или кнопку "Save"
2. **Toggle справа сверху** → включи (станет зелёным ✅)
3. Должно быть написано **"Active"**

---

## ✅ Проверка

### Проверка 1: Webhook отвечает
```bash
curl -I https://guglovskij.app.n8n.cloud/webhook/07a96af0-9f1f-44e7-bad3-86a2c4e0cb28
```

Ожидается:
```
HTTP/2 302
location: https://accounts.google.com/o/oauth2/v2/auth?...
```

### Проверка 2: Через браузер
Открой в браузере:
```
https://guglovskij.app.n8n.cloud/webhook/07a96af0-9f1f-44e7-bad3-86a2c4e0cb28
```

Должен перенаправить на страницу Google OAuth.

---

## 🔍 Troubleshooting

### Ошибка: 404 "webhook not registered"

**Причина:** Workflow не активирован

**Решение:**
1. Открой workflow в n8n
2. Проверь что toggle справа сверху **зелёный** ✅
3. В списке workflows рядом с названием должна быть зелёная галочка

---

### Ошибка: Workflow запущен, но не работает

**Причина:** Неправильный путь в Webhook node

**Решение:**
1. Открой Webhook node
2. Проверь Path: `07a96af0-9f1f-44e7-bad3-86a2c4e0cb28`
3. Должен быть **БЕЗ** слеша в начале
4. **Сохрани** изменения

---

### Временное решение: Test URL

Если Production URL не работает:

1. В Webhook node нажми **"Listen for Test Event"**
2. Скопируй **Test URL** (будет типа `webhook-test/...`)
3. Замени в `GoogleOAuthService.java`:
   ```java
   private static final String N8N_WEBHOOK_URL = 
       "https://guglovskij.app.n8n.cloud/webhook-test/07a96af0-9f1f-44e7-bad3-86a2c4e0cb28";
   ```
4. Пересобери backend

⚠️ **Test URL работает БЕЗ активации workflow!**

---

## 📊 Схема Workflow

```
┌──────────────┐
│   Webhook    │  GET request
│     (GET)    │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│     Code     │  Build Google OAuth URL
│  (JavaScript)│
└──────┬───────┘
       │
       ▼
┌──────────────┐
│   Respond    │  HTTP 302 Redirect
│  to Webhook  │  → Google OAuth
└──────────────┘
```

---

## 🎯 После настройки n8n

### 1. Проверь что workflow активен (зелёный toggle)

### 2. Протестируй webhook:
```bash
curl -I https://guglovskij.app.n8n.cloud/webhook/07a96af0-9f1f-44e7-bad3-86a2c4e0cb28
```

### 3. Пересобери backend:
```bash
./gradlew clean build -x test
```

### 4. Задеплой на сервер

### 5. Тест авторизации:
- Открой сайт
- Нажми "Войти через Google"
- Должен пройти через n8n → Google → обратно

---

## ✅ Всё готово!

После активации workflow авторизация через Google заработает! 🚀

**Удачи!**

