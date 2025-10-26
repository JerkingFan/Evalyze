# 🚀 Google OAuth через n8n - Старт

## ✅ Что было сделано

Авторизация Google успешно переделана для работы через **n8n webhook** вместо прямой интеграции с Google OAuth API.

**Webhook URL:** `https://guglovskij.app.n8n.cloud/webhook/07a96af0-9f1f-44e7-bad3-86a2c4e0cb28`

---

## 📂 Изменённые файлы

### Backend (Java/Spring Boot)
- ✅ `GoogleOAuthService.java` - упрощён для работы с n8n
- ✅ `GoogleOAuthController.java` - новые endpoints
- ✅ `application.properties` - закомментированы старые OAuth настройки
- ✅ `build.gradle` - закомментирована зависимость oauth2-client

### Frontend (JavaScript)
- ✅ `app.js` - обновлены методы авторизации

### Документация
- 📄 `GOOGLE_OAUTH_N8N.md` - полная документация
- 📄 `GOOGLE_OAUTH_N8N_SUMMARY.md` - краткое резюме
- 📄 `N8N_WEBHOOK_SETUP.md` - настройка n8n
- 📄 `START_HERE.md` - этот файл

---

## 🎯 Что нужно сделать дальше

### Шаг 1: Настроить n8n Workflow
📖 **Читайте:** `N8N_WEBHOOK_SETUP.md`

Кратко:
1. Создайте workflow в n8n
2. Добавьте Webhook node
3. Добавьте Google OAuth node
4. Настройте redirect на backend

### Шаг 2: Перезапустить backend
```bash
# Убедитесь что все зависимости актуальны
./gradlew clean build

# Запустите приложение
./gradlew bootRun
```

### Шаг 3: Протестировать
1. Откройте: `http://5.83.140.54:8089`
2. Нажмите "Войти через Google"
3. Должна пройти авторизация через n8n

---

## 🔍 Быстрая проверка

### Проверка 1: Backend доступен
```bash
curl http://5.83.140.54:8089/api/auth/google/url
# Ожидается: https://guglovskij.app.n8n.cloud/webhook/07a96af0-9f1f-44e7-bad3-86a2c4e0cb28
```

### Проверка 2: n8n webhook отвечает
```bash
curl https://guglovskij.app.n8n.cloud/webhook/07a96af0-9f1f-44e7-bad3-86a2c4e0cb28
# Не должно быть 404
```

### Проверка 3: Тест через браузер
1. Откройте DevTools (F12)
2. Перейдите во вкладку Network
3. Нажмите "Войти через Google"
4. Следите за запросами и редиректами

---

## 📖 Документация

| Файл | Описание |
|------|----------|
| `GOOGLE_OAUTH_N8N.md` | 📚 Полная документация по архитектуре и API |
| `GOOGLE_OAUTH_N8N_SUMMARY.md` | 📝 Краткое резюме всех изменений |
| `N8N_WEBHOOK_SETUP.md` | ⚙️ Пошаговая настройка n8n workflow |
| `START_HERE.md` | 🚀 Этот файл - быстрый старт |

---

## 🔧 API Endpoints (изменения)

### Новый endpoint
```
POST /api/auth/google/authenticate
Body: { "token": "google_access_token" }
Response: JWT token и данные пользователя
```

### Изменённые endpoints
```
GET /api/auth/google/url
→ Теперь возвращает n8n webhook URL

GET /api/auth/google/callback
→ Теперь обрабатывает callback от n8n
```

---

## ⚠️ Важные замечания

1. **n8n workflow ОБЯЗАТЕЛЕН** - без него авторизация не будет работать
2. **Google credentials** теперь настраиваются в n8n, НЕ в backend
3. **JWT токены** по-прежнему генерируются на backend
4. **Существующие пользователи** могут войти через новую систему

---

## 🐛 Что делать если не работает

### Ошибка: "OAuth via n8n failed"
1. Проверьте логи backend
2. Проверьте что n8n workflow активен
3. Проверьте URL webhook

### Ошибка: "Email not received"
1. Проверьте что n8n возвращает поле "email"
2. Проверьте формат ответа от n8n
3. См. примеры в `N8N_WEBHOOK_SETUP.md`

### Ошибка: CORS
1. Настройте CORS в n8n webhook settings
2. Проверьте что backend разрешает запросы от n8n

### Ошибка: 404 на callback
1. Убедитесь что backend запущен
2. Проверьте URL в n8n redirect

---

## 💡 Подсказки

### Просмотр логов backend
```bash
# В консоли Spring Boot ищите:
=== Google OAuth via n8n START ===
Token received: ...
n8n webhook response: ...
User info from n8n: email=...
=== Google OAuth via n8n SUCCESS ===
```

### Отладка в браузере
1. F12 → Network tab
2. Следите за редиректами
3. Проверяйте параметры URL
4. Смотрите localStorage после авторизации

---

## 🎉 Готово к работе!

После настройки n8n workflow всё должно работать автоматически.

**Следующие шаги:**
1. ✅ Настройте n8n (`N8N_WEBHOOK_SETUP.md`)
2. ✅ Перезапустите backend
3. ✅ Протестируйте авторизацию
4. ✅ При необходимости - отладка по логам

---

**Вопросы?** Читайте полную документацию в `GOOGLE_OAUTH_N8N.md`

**Удачи! 🚀**

