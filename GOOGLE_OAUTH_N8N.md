# 🔐 Google OAuth через n8n Webhook

## 📋 Обзор

Авторизация Google теперь работает через webhook n8n вместо прямой интеграции с Google OAuth API.

**URL webhook:** `https://guglovskij.app.n8n.cloud/webhook/07a96af0-9f1f-44e7-bad3-86a2c4e0cb28`

---

## 🏗️ Архитектура

### Старая схема (убрана)
```
Frontend → Backend → Google OAuth API → Backend → Frontend
```

### Новая схема (текущая)
```
Frontend → n8n Webhook → Backend → Frontend
     ИЛИ
Frontend → Backend → n8n Webhook → Backend → Frontend
```

---

## 🔄 Варианты авторизации

### Вариант 1: Прямой редирект на n8n (рекомендуется)
1. Пользователь нажимает кнопку "Войти через Google"
2. Frontend делает запрос на `/api/auth/google/url`
3. Backend возвращает URL webhook n8n
4. Frontend перенаправляет пользователя на n8n webhook
5. n8n обрабатывает OAuth с Google
6. n8n возвращает callback на `/api/auth/google/callback?token=...`
7. Backend создаёт/находит пользователя и генерирует JWT
8. Пользователь перенаправляется на главную страницу с токеном

### Вариант 2: Получение токена через POST
1. Пользователь как-то получает Google токен (например, через Google Sign-In библиотеку)
2. Frontend отправляет POST запрос на `/api/auth/google/authenticate` с токеном
3. Backend отправляет токен в n8n webhook
4. n8n возвращает данные пользователя
5. Backend создаёт/находит пользователя и генерирует JWT
6. Frontend получает JWT и сохраняет в localStorage

---

## 📡 API Endpoints

### 1. GET `/api/auth/google/url`
**Описание:** Получить URL webhook n8n для авторизации

**Ответ:**
```
https://guglovskij.app.n8n.cloud/webhook/07a96af0-9f1f-44e7-bad3-86a2c4e0cb28
```

### 2. POST `/api/auth/google/authenticate`
**Описание:** Аутентифицировать пользователя с Google токеном

**Запрос:**
```json
{
  "token": "google_access_token_here"
}
```

**Ответ:**
```json
{
  "token": "jwt_token_here",
  "email": "user@example.com",
  "role": "EMPLOYEE",
  "fullName": "John Doe",
  "companyId": null,
  "message": "Login successful"
}
```

### 3. GET `/api/auth/google/callback`
**Описание:** Callback endpoint для n8n webhook

**Параметры:**
- `token` - Google access token от n8n
- `email` (optional) - email пользователя
- `name` (optional) - имя пользователя

**Поведение:** Редирект на главную страницу с параметрами аутентификации

---

## 🔧 Настройка n8n Webhook

### Ожидаемые данные от n8n

**Вариант 1: GET Callback**
```
/api/auth/google/callback?token=google_token&email=user@example.com&name=John+Doe
```

**Вариант 2: POST Response**
```json
{
  "email": "user@example.com",
  "name": "John Doe",
  "picture": "https://..."
}
```

### Структура n8n workflow

1. **Webhook Trigger** - принимает запрос от frontend
2. **Google OAuth Node** - выполняет авторизацию с Google
3. **HTTP Response** - возвращает данные пользователя или делает редирект

---

## 💻 Frontend Integration

### Метод 1: Использование loginWithGoogle()
```javascript
// В HTML
<button onclick="app.loginWithGoogle()">Войти через Google</button>

// В app.js (уже реализовано)
async loginWithGoogle() {
    const response = await fetch('/api/auth/google/url');
    const webhookUrl = await response.text();
    window.location.href = webhookUrl;
}
```

### Метод 2: Прямая отправка токена
```javascript
// Если у вас уже есть Google токен
const googleToken = "ya29.a0AfH6SMCY...";
await app.authenticateWithGoogleToken(googleToken);
```

---

## 🗂️ Изменённые файлы

### Backend
- ✅ `GoogleOAuthService.java` - упрощён для работы с n8n
- ✅ `GoogleOAuthController.java` - новые endpoints для n8n
- ✅ `application.properties` - закомментированы старые OAuth настройки

### Frontend
- ✅ `app.js` - обновлён метод `loginWithGoogle()`
- ✅ `app.js` - добавлен метод `authenticateWithGoogleToken()`

---

## 🔍 Отладка

### Логи в backend
```
=== Google OAuth via n8n START ===
Token received: ya29.a0AfH6SMCY...
n8n webhook response: {"email":"user@example.com","name":"John Doe"}
User info from n8n: email=user@example.com, name=John Doe
User found/created: user@example.com
=== Google OAuth via n8n SUCCESS ===
```

### Проверка в браузере
1. Откройте Developer Tools (F12)
2. Перейдите во вкладку Network
3. Нажмите "Войти через Google"
4. Следите за запросами:
   - `/api/auth/google/url`
   - Редирект на n8n webhook
   - `/api/auth/google/callback` (если используется)

---

## ⚠️ Важные замечания

1. **n8n webhook должен быть настроен** для обработки Google OAuth
2. **CORS настройки** n8n должны разрешать запросы с вашего домена
3. **Google Client ID и Secret** настраиваются в n8n, не в backend
4. **JWT токен генерируется на backend**, не в n8n
5. **Роль пользователя по умолчанию** - EMPLOYEE (можно изменить в коде)

---

## 🚀 Следующие шаги

1. Настройте n8n workflow для Google OAuth
2. Проверьте, что webhook возвращает правильные данные
3. Протестируйте авторизацию через браузер
4. При необходимости обновите структуру ответа от n8n
5. Добавьте обработку ошибок в n8n workflow

---

## 📞 Поддержка

Если возникают проблемы:
1. Проверьте логи backend (console output)
2. Проверьте логи n8n workflow
3. Убедитесь что webhook URL доступен
4. Проверьте формат данных, возвращаемых от n8n

