# 🚀 Быстрый деплой Google OAuth

## ✅ Что сделано:

### Backend:
1. ✅ Добавлена зависимость `spring-boot-starter-oauth2-client`
2. ✅ Настроен `application.properties` с Google credentials
3. ✅ Создан `GoogleOAuthController` с endpoints
4. ✅ Создан `GoogleOAuthService` для обработки OAuth flow
5. ✅ SecurityConfig уже разрешает доступ к `/api/auth/**`

### Frontend:
1. ✅ Добавлена кнопка "Войти через Google" в модальное окно логина
2. ✅ Добавлен метод `loginWithGoogle()` в `app.js`
3. ✅ Добавлен `handleOAuthCallback()` для обработки редиректа
4. ✅ Автоматическое сохранение токена и перенаправление

## 🔧 Перед деплоем:

### Настройте Google Cloud Console:
1. **Authorized redirect URIs**:
   ```
   http://5.83.140.54:8089/api/auth/google/callback
   ```

2. **OAuth consent screen**:
   - Добавьте scopes:
     - `openid`
     - `profile`
     - `email`
   
   ⚠️ **Примечание:** `drive.readonly` требует верификации Google.
   Для тестирования добавьте себя в **Test users** или используйте только базовые scopes.
   
   См. `GOOGLE_OAUTH_SCOPES.md` для подробностей.

## 📦 Деплой:

```bash
# 1. Пересоберите
./gradlew clean build -x test

# 2. Загрузите на сервер
scp build/libs/NEW_NEW_MVP-0.0.1-SNAPSHOT.jar root@5.83.140.54:~/start.jar

# 3. Перезапустите
ssh root@5.83.140.54 "pkill -f start.jar && nohup java -jar start.jar > app.log 2>&1 &"

# 4. Проверьте логи
ssh root@5.83.140.54 "tail -f app.log"
```

## 🧪 Тестирование:

1. **Откройте:** http://5.83.140.54:8089/
2. **Нажмите:** "Войти"
3. **Нажмите:** "Войти через Google"
4. **Авторизуйтесь** в Google
5. **Результат:** Перенаправление на `/profile` с сохраненным токеном

## 📊 Что ожидать в логах:

```
=== Google OAuth Callback START ===
Authorization code: 4/0AanRR...
Access token obtained: ya29.a0AfB_by...
User info: email=test@gmail.com, name=Test User
User found/created: test@gmail.com
=== Google OAuth Callback SUCCESS ===
```

## 🎯 API Endpoints:

- **GET** `/api/auth/google/url` - получить authorization URL
- **GET** `/api/auth/google/callback?code=...` - callback от Google

## ⚠️ Важно:

- **Client Secret** можно оставить пустым для публичных приложений
- **Redirect URI** в коде: `http://5.83.140.54:8089/api/auth/google/callback`
- При первой авторизации создается новый пользователь с ролью `EMPLOYEE`

## 📝 Документация:

Полная документация: `GOOGLE_OAUTH_SETUP.md`

