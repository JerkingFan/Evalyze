# 🚀 Полная настройка Google OAuth через GitHub Pages + n8n

## 📋 Чек-лист

- [ ] GitHub репозиторий создан
- [ ] GitHub Pages включены
- [ ] Google Cloud Console настроен
- [ ] n8n workflow создан и активирован
- [ ] Backend обновлён

---

## 🐙 Шаг 1: GitHub Pages

### 1.1 Создай репозиторий
```bash
# Создай новый репозиторий на GitHub
# Название: oauth-bridge (или любое другое)
```

### 1.2 Загрузи файл
1. Скопируй содержимое `github-pages-bridge.html`
2. Создай файл `index.html` в корне репозитория
3. Вставь код из `github-pages-bridge.html`

### 1.3 Включи GitHub Pages
1. Зайди в **Settings** → **Pages**
2. **Source**: Deploy from a branch
3. **Branch**: main
4. **Folder**: / (root)
5. Сохрани

### 1.4 Получи URL
Твой URL будет: `https://YOUR_USERNAME.github.io/oauth-bridge/`

**ВАЖНО:** Замени `YOUR_USERNAME` и `oauth-bridge` на свои значения!

---

## 🔐 Шаг 2: Google Cloud Console

### 2.1 Создай новый Client ID
1. Зайди в [Google Cloud Console](https://console.cloud.google.com/)
2. **APIs & Services** → **Credentials**
3. **Create Credentials** → **OAuth 2.0 Client ID**
4. **Application type**: Web application
5. **Name**: OAuth Bridge

### 2.2 Настрой Authorized redirect URIs
**Добавь ТОЛЬКО один URL:**
```
https://YOUR_USERNAME.github.io/oauth-bridge/
```

**ВАЖНО:** URL должен точно совпадать с GitHub Pages!

### 2.3 Включи Google Drive API
1. **APIs & Services** → **Library**
2. Найди "Google Drive API"
3. **Enable**

### 2.4 Скопируй данные
- **Client ID**: `340752343067-79ipapn7o97qd8ibqvgpjg4687fm7jo7.apps.googleusercontent.com`
- **Client Secret**: `GOCSPX-...` (скопируй свой)

---

## 🔧 Шаг 3: n8n Workflow

### 3.1 Создай новый workflow
1. Зайди в [n8n](https://guglovskij.app.n8n.cloud/)
2. **New workflow**
3. Название: "OAuth Bridge Handler"

### 3.2 Webhook Trigger
1. **Add node** → **Webhook**
2. **HTTP Method**: POST
3. **Path**: `oauth-callback`
4. **Production URL**: Скопируй и сохрани!

### 3.3 HTTP Request - Token Exchange
1. **Add node** → **HTTP Request**
2. **Method**: POST
3. **URL**: `https://oauth2.googleapis.com/token`
4. **Headers**:
   ```
   Content-Type: application/x-www-form-urlencoded
   ```
5. **Body** (Form-Data):
   ```
   client_id: 340752343067-79ipapn7o97qd8ibqvgpjg4687fm7jo7.apps.googleusercontent.com
   client_secret: YOUR_CLIENT_SECRET
   code: {{ $json.code }}
   grant_type: authorization_code
   redirect_uri: {{ $json.redirect_uri }}
   ```

### 3.4 HTTP Request - User Info
1. **Add node** → **HTTP Request**
2. **Method**: GET
3. **URL**: `https://www.googleapis.com/oauth2/v2/userinfo`
4. **Headers**:
   ```
   Authorization: Bearer {{ $json.access_token }}
   ```

### 3.5 HTTP Request - Backend
1. **Add node** → **HTTP Request**
2. **Method**: POST
3. **URL**: `http://5.83.140.54:8089/api/auth/google/authenticate`
4. **Headers**:
   ```
   Content-Type: application/json
   ```
5. **Body**:
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

### 3.6 Respond to Webhook
1. **Add node** → **Respond to Webhook**
2. **Respond With**: Text
3. **Response Body**: `{"status": "ok"}`

### 3.7 Соедини ноды
```
Webhook → Token Exchange → User Info → Backend → Respond
```

### 3.8 Активируй workflow
1. **Save** workflow
2. **Toggle** в правом верхнем углу (должен стать зелёным)
3. **Скопируй** Production URL

---

## 🔄 Шаг 4: Обнови код

### 4.1 Обнови GitHub Pages bridge
В файле `index.html` на GitHub замени:
```javascript
const N8N_WEBHOOK_URL = 'https://guglovskij.app.n8n.cloud/webhook/07a96af0-9f1f-44e7-bad3-86a2c4e0cb28';
```

На свой Production URL из n8n.

### 4.2 Обнови фронтенд
В файле `src/main/resources/static/js/app.js` замени:
```javascript
const githubPagesUrl = 'https://YOUR_USERNAME.github.io/YOUR_REPO_NAME/github-pages-bridge.html';
```

На свой GitHub Pages URL.

### 4.3 Обнови backend (если нужно)
Убедись что endpoint `/api/auth/google/authenticate` существует и принимает:
```json
{
  "googleToken": "string",
  "userInfo": {
    "email": "string",
    "name": "string",
    "picture": "string"
  },
  "refreshToken": "string",
  "scopes": "string"
}
```

---

## 🧪 Шаг 5: Тестирование

### 5.1 Проверь GitHub Pages
Открой: `https://YOUR_USERNAME.github.io/oauth-bridge/`

Должен произойти редирект на Google OAuth.

### 5.2 Проверь полный flow
1. Нажми "Войти через Google" на твоём сайте
2. Должен произойти редирект на GitHub Pages
3. GitHub Pages перенаправит на Google
4. После авторизации - обратно на GitHub Pages
5. GitHub Pages отправит данные в n8n
6. n8n обработает и отправит в backend
7. Редирект на твой сайт с успехом

### 5.3 Проверь логи
- **GitHub Pages**: Открой DevTools → Console
- **n8n**: Зайди в Executions
- **Backend**: Проверь логи приложения

---

## 🔧 Troubleshooting

### Ошибка: "Invalid redirect_uri"
**Решение:**
1. Проверь Google Cloud Console
2. URL должен точно совпадать с GitHub Pages
3. Не должно быть лишних слешей или параметров

### Ошибка: "Workflow not found"
**Решение:**
1. Убедись что workflow АКТИВЕН
2. Проверь Production URL
3. Попробуй Test URL для проверки

### Ошибка: "Connection refused"
**Решение:**
1. Проверь что backend запущен
2. Проверь firewall
3. Проверь URL в n8n workflow

### Ошибка: "Invalid client"
**Решение:**
1. Проверь Client ID и Secret
2. Убедись что OAuth consent screen настроен
3. Проверь что Google Drive API включён

---

## ✅ Финальная проверка

1. **GitHub Pages работает**: `https://YOUR_USERNAME.github.io/oauth-bridge/`
2. **Google OAuth работает**: Редирект на Google происходит
3. **n8n workflow активен**: Зелёный toggle
4. **Backend доступен**: `http://5.83.140.54:8089/`
5. **Полный flow работает**: От кнопки до успешного входа

**Если всё работает - OAuth настроен правильно!** 🎉
