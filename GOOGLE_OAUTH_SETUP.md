# 🔐 Google OAuth 2.0 - Полная настройка

## ✅ Реализовано

### 1. **Backend (Spring Boot)**

#### `application.properties` - Конфигурация
```properties
# Google OAuth2 Configuration
spring.security.oauth2.client.registration.google.client-id=340752343067-79ipapn7o97qd8ibqvgpjg4687fm7jo7.apps.googleusercontent.com
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET:}
spring.security.oauth2.client.registration.google.scope=openid,profile,email,https://www.googleapis.com/auth/drive.readonly
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/api/auth/google/callback
spring.security.oauth2.client.registration.google.authorization-grant-type=authorization_code

# Google OAuth2 Provider Configuration
spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/v2/auth
spring.security.oauth2.client.provider.google.token-uri=https://oauth2.googleapis.com/token
spring.security.oauth2.client.provider.google.user-info-uri=https://www.googleapis.com/oauth2/v3/userinfo
spring.security.oauth2.client.provider.google.user-name-attribute=sub
```

**Параметры:**
- `client_id` - ваш Client ID из Google Cloud Console
- `client_secret` - secret ключ (может быть пустым для публичных приложений)
- `scope` - запрашиваемые права доступа:
  - `openid` - базовая информация
  - `profile` - имя пользователя
  - `email` - email адрес
  - `https://www.googleapis.com/auth/drive.readonly` - чтение Google Drive

#### `build.gradle` - Зависимости
```gradle
implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
```

#### `GoogleOAuthController.java` - API Endpoints
- **GET** `/api/auth/google/url` - получить URL для авторизации
- **GET** `/api/auth/google/callback` - callback после авторизации Google
- **POST** `/api/auth/google/token` - альтернативный способ обмена кода на токен

#### `GoogleOAuthService.java` - Бизнес-логика
- Генерация authorization URL
- Обмен authorization code на access token
- Получение информации о пользователе из Google API
- Поиск или создание пользователя в БД
- Генерация JWT токена для приложения

### 2. **Frontend (JavaScript)**

#### `app.js` - Методы
```javascript
async loginWithGoogle() {
    // 1. Запрашиваем URL авторизации у сервера
    const response = await fetch('/api/auth/google/url');
    const authUrl = await response.text();
    
    // 2. Перенаправляем на страницу Google
    window.location.href = authUrl;
}

handleOAuthCallback() {
    // 1. Проверяем параметры в URL после редиректа
    const token = urlParams.get('token');
    const email = urlParams.get('email');
    
    // 2. Сохраняем в localStorage
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify({ email, role, fullName }));
    
    // 3. Перенаправляем на нужную страницу
    if (role === 'COMPANY') {
        window.location.href = '/company';
    } else {
        window.location.href = '/profile';
    }
}
```

#### `index.html` - Кнопка Google
```html
<button type="button" class="btn w-100" onclick="app.loginWithGoogle()" 
        style="background: white; color: #333; border: none; font-weight: 500;">
    <svg><!-- Google logo SVG --></svg>
    Войти через Google
</button>
```

## 🔄 Поток авторизации

```
[Пользователь]
    ↓ 1. Нажимает "Войти через Google"
[Frontend] 
    ↓ 2. GET /api/auth/google/url
[Backend]
    ↓ 3. Возвращает Google Auth URL
[Frontend]
    ↓ 4. Перенаправляет на Google
[Google]
    ↓ 5. Пользователь авторизуется
[Google]
    ↓ 6. Redirect /api/auth/google/callback?code=...
[Backend]
    ↓ 7. Обменивает code на access_token
[Backend]
    ↓ 8. Получает user info из Google API
[Backend]
    ↓ 9. Находит/создает пользователя в БД
[Backend]
    ↓ 10. Генерирует JWT токен
[Backend]
    ↓ 11. Redirect /?token=...&email=...&role=...
[Frontend]
    ↓ 12. Сохраняет токен в localStorage
[Frontend]
    ↓ 13. Перенаправляет на /profile или /company
[Пользователь] - Авторизован!
```

## 🔧 Настройка Google Cloud Console

### Шаг 1: Создайте проект
1. Перейдите: https://console.cloud.google.com/
2. Создайте новый проект или выберите существующий

### Шаг 2: Включите Google+ API
1. **APIs & Services** → **Library**
2. Найдите **Google+ API**
3. Нажмите **Enable**

### Шаг 3: Создайте OAuth 2.0 credentials
1. **APIs & Services** → **Credentials**
2. **Create Credentials** → **OAuth client ID**
3. **Application type** → **Web application**
4. **Authorized redirect URIs**:
   ```
   http://localhost:8089/api/auth/google/callback
   http://5.83.140.54:8089/api/auth/google/callback
   ```

### Шаг 4: Получите credentials
- **Client ID**: `340752343067-79ipapn7o97qd8ibqvgpjg4687fm7jo7.apps.googleusercontent.com`
- **Client Secret**: (опционально, для публичных приложений можно оставить пустым)

### Шаг 5: Настройте OAuth consent screen
1. **User Type** → **External**
2. **App name** → `Evalyze`
3. **Support email** → ваш email
4. **Scopes**:
   - `openid`
   - `profile`
   - `email`
   - `https://www.googleapis.com/auth/drive.readonly`

## 📝 Обновленные файлы

### Backend:
- ✅ `src/main/resources/application.properties`
- ✅ `build.gradle`
- ✅ `src/main/java/.../controller/GoogleOAuthController.java` (новый)
- ✅ `src/main/java/.../service/GoogleOAuthService.java` (новый)

### Frontend:
- ✅ `src/main/resources/static/js/app.js`
- ✅ `src/main/resources/templates/index.html`

## 🚀 Деплой

### 1. Пересоберите приложение:
```bash
./gradlew clean build -x test
```

### 2. Загрузите на сервер:
```bash
scp build/libs/NEW_NEW_MVP-0.0.1-SNAPSHOT.jar root@5.83.140.54:~/start.jar
```

### 3. Перезапустите:
```bash
ssh root@5.83.140.54
pkill -f start.jar
nohup java -jar start.jar > app.log 2>&1 &
tail -f app.log
```

## 🧪 Тестирование

### 1. Откройте главную страницу:
```
http://5.83.140.54:8089/
```

### 2. Нажмите "Войти" → "Войти через Google"

### 3. Авторизуйтесь в Google

### 4. После успешной авторизации:
- Вы будете перенаправлены на `/profile` (для EMPLOYEE)
- Или на `/company` (для COMPANY)
- Токен сохранен в localStorage

### 5. Проверьте логи:
```bash
ssh root@5.83.140.54 "tail -100 app.log | grep Google"
```

**Ожидаемые логи:**
```
=== Google OAuth Callback START ===
Authorization code: 4/0AanRR...
Access token obtained: ya29.a0AfB_by...
User info: email=test@gmail.com, name=Test User
User found/created: test@gmail.com
=== Google OAuth Callback SUCCESS ===
```

## ⚠️ Важно

### 1. Client Secret (опционально)
Для публичных веб-приложений можно не использовать `client_secret`. 
Если Google требует, добавьте переменную окружения:
```bash
export GOOGLE_CLIENT_SECRET="your-secret"
```

### 2. Redirect URI
Убедитесь, что redirect URI в коде совпадает с настройками в Google Console:
```java
// GoogleOAuthService.java
private String getRedirectUri() {
    return "http://5.83.140.54:8089/api/auth/google/callback";
}
```

### 3. Scopes
Приложение запрашивает доступ к Google Drive (readonly). 
Пользователь должен одобрить эти права при авторизации.

### 4. Первая авторизация
При первой авторизации создается новый пользователь с:
- Email из Google
- Full Name из Google
- Role = EMPLOYEE (по умолчанию)
- Google OAuth Token сохраняется в БД

## 🔍 Отладка

### Проблема: "redirect_uri_mismatch"
**Решение:** Проверьте, что redirect URI в Google Console совпадает с тем, что указан в приложении.

### Проблема: "invalid_client"
**Решение:** Проверьте Client ID и Client Secret.

### Проблема: "access_denied"
**Решение:** Пользователь отменил авторизацию или не одобрил запрашиваемые права.

### Проблема: JWT токен не генерируется
**Решение:** Проверьте `JwtUtil` и настройки в `application.properties`:
```properties
jwt.secret=your-secret-key
jwt.expiration=86400000
```

## ✅ Готово!

Авторизация через Google полностью реализована и готова к использованию! 🎉

