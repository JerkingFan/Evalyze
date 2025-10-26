# 🔧 n8n Workflow для GitHub Pages OAuth Bridge

## 📋 Схема Workflow

```
Webhook Trigger → HTTP Request (Google Token Exchange) → Supabase Update → Respond to Webhook
```

## 🛠️ Настройка Workflow

### 1. Webhook Trigger
- **Name**: `OAuth Callback Handler`
- **HTTP Method**: `POST`
- **Path**: `oauth-callback`
- **Production URL**: `https://guglovskij.app.n8n.cloud/webhook/07a96af0-9f1f-44e7-bad3-86a2c4e0cb28`

**Входящие данные от GitHub Pages bridge:**
```json
{
  "code": "4/0AX4XfWh...",
  "state": "1234567890",
  "redirect_uri": "https://YOUR_USERNAME.github.io/YOUR_REPO_NAME/github-pages-bridge.html"
}
```

### 2. HTTP Request - "Обмен кода на токен"

**Настройки:**
- **Method**: `POST`
- **URL**: `https://oauth2.googleapis.com/token`
- **Headers**:
  ```
  Content-Type: application/x-www-form-urlencoded
  ```

**Body (Form-Data):**
```
client_id: 340752343067-79ipapn7o97qd8ibqvgpjg4687fm7jo7.apps.googleusercontent.com
client_secret: YOUR_CLIENT_SECRET
code: {{ $json.code }}
grant_type: authorization_code
redirect_uri: {{ $json.redirect_uri }}
```

**Ожидаемый ответ:**
```json
{
  "access_token": "ya29.a0AfH6SMC...",
  "expires_in": 3599,
  "refresh_token": "1//04...",
  "scope": "openid profile email https://www.googleapis.com/auth/drive.readonly",
  "token_type": "Bearer"
}
```

### 3. HTTP Request - "Получение данных пользователя"

**Настройки:**
- **Method**: `GET`
- **URL**: `https://www.googleapis.com/oauth2/v2/userinfo`
- **Headers**:
  ```
  Authorization: Bearer {{ $json.access_token }}
  ```

**Ожидаемый ответ:**
```json
{
  "id": "123456789",
  "email": "user@example.com",
  "verified_email": true,
  "name": "John Doe",
  "given_name": "John",
  "family_name": "Doe",
  "picture": "https://lh3.googleusercontent.com/..."
}
```

### 4. HTTP Request - "Отправка данных на ваш backend"

**Настройки:**
- **Method**: `POST`
- **URL**: `http://5.83.140.54:8089/api/auth/google/authenticate`
- **Headers**:
  ```
  Content-Type: application/json
  ```

**Body:**
```json
{
  "googleToken": "{{ $json.access_token }}",
  "userInfo": {
    "email": "{{ $json.email }}",
    "name": "{{ $json.name }}",
    "picture": "{{ $json.picture }}"
  },
  "refreshToken": "{{ $json.refresh_token }}",
  "scopes": "{{ $json.scope }}"
}
```

### 5. Respond to Webhook

**Настройки:**
- **Respond With**: `Text`
- **Response Body**: `{"status": "ok", "message": "OAuth processed successfully"}`

## 🔧 Код для Code Node (альтернатива)

Если нужна дополнительная обработка, добавь Code Node между HTTP Request и Respond:

```javascript
// Обработка данных от Google
const googleData = $input.first().json;

// Логика обработки
const processedData = {
  status: 'success',
  user: {
    email: googleData.email,
    name: googleData.name,
    picture: googleData.picture
  },
  tokens: {
    access_token: googleData.access_token,
    refresh_token: googleData.refresh_token
  }
};

return processedData;
```

## 🚀 Активация Workflow

1. **Сохрани** workflow
2. **Активируй** toggle в правом верхнем углу
3. **Скопируй** Production URL
4. **Вставь** URL в `github-pages-bridge.html`

## 🔍 Тестирование

### Тест 1: Проверка Webhook
```bash
curl -X POST https://guglovskij.app.n8n.cloud/webhook/07a96af0-9f1f-44e7-bad3-86a2c4e0cb28 \
  -H "Content-Type: application/json" \
  -d '{"code":"test","state":"123","redirect_uri":"https://example.com"}'
```

### Тест 2: Полный OAuth Flow
1. Открой `https://YOUR_USERNAME.github.io/YOUR_REPO_NAME/github-pages-bridge.html`
2. Должен произойти редирект на Google
3. После авторизации - редирект обратно
4. Проверь логи в n8n

## ⚠️ Важные моменты

1. **redirect_uri** в Google Cloud Console должен точно совпадать с URL GitHub Pages
2. **Client Secret** должен быть правильным
3. **Workflow должен быть АКТИВЕН**
4. **Все HTTP Request ноды должны быть правильно настроены**

## 🔧 Troubleshooting

### Ошибка: "Invalid redirect_uri"
- Проверь что в Google Cloud Console указан правильный URL GitHub Pages
- URL должен быть точно: `https://YOUR_USERNAME.github.io/YOUR_REPO_NAME/github-pages-bridge.html`

### Ошибка: "Invalid client"
- Проверь Client ID и Client Secret
- Убедись что OAuth consent screen настроен

### Ошибка: "Workflow not found"
- Убедись что workflow АКТИВЕН
- Проверь Production URL

### Ошибка: "Connection refused" к backend
- Проверь что backend запущен на `5.83.140.54:8089`
- Проверь firewall и доступность порта
