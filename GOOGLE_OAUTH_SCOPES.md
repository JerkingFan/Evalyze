# 🔐 Google OAuth Scopes - Настройка

## ⚠️ Проблема: "Error 400: invalid_request"

### Причина:
Google требует **верификацию приложения** для использования расширенных scopes, таких как:
- `https://www.googleapis.com/auth/drive.readonly`
- `https://www.googleapis.com/auth/drive`
- и других чувствительных API

Для **неверифицированных** приложений доступны только **базовые scopes**.

## ✅ Текущая конфигурация (базовые scopes):

```properties
# application.properties
spring.security.oauth2.client.registration.google.scope=openid,profile,email
```

### Доступные данные:
- ✅ `openid` - базовая идентификация
- ✅ `profile` - имя пользователя
- ✅ `email` - email адрес

Этих scopes **достаточно** для:
- Регистрации пользователя
- Входа через Google
- Получения базовой информации о профиле

## 🔓 Как добавить `drive.readonly`:

### Вариант 1: Верификация приложения (рекомендуется для продакшена)

#### Шаг 1: Подготовьте документы
Google требует:
1. **Домен приложения** (должен быть зарегистрирован на вас)
2. **Политика конфиденциальности** (Privacy Policy)
3. **Условия использования** (Terms of Service)
4. **Логотип приложения**
5. **Скриншоты приложения**

#### Шаг 2: Подайте заявку на верификацию
1. Перейдите в [Google Cloud Console](https://console.cloud.google.com/)
2. Выберите ваш проект
3. **APIs & Services** → **OAuth consent screen**
4. Нажмите **Publish App**
5. Заполните форму верификации
6. Отправьте на проверку

⏱️ **Время проверки:** 3-7 дней

#### Шаг 3: После одобрения
Обновите `application.properties`:
```properties
spring.security.oauth2.client.registration.google.scope=openid,profile,email,https://www.googleapis.com/auth/drive.readonly
```

### Вариант 2: Тестовые пользователи (для разработки)

Для **неверифицированных** приложений можно добавить до **100 тестовых пользователей**:

#### Шаг 1: Добавьте тестовых пользователей
1. [Google Cloud Console](https://console.cloud.google.com/)
2. **APIs & Services** → **OAuth consent screen**
3. Прокрутите до **Test users**
4. Нажмите **Add Users**
5. Добавьте email адреса (до 100)

#### Шаг 2: Обновите scopes
```properties
spring.security.oauth2.client.registration.google.scope=openid,profile,email,https://www.googleapis.com/auth/drive.readonly
```

⚠️ **Ограничения:**
- Работает только для указанных email адресов
- Максимум 100 тестовых пользователей
- Показывается предупреждение "Unverified app"

### Вариант 3: Internal App (для организаций Google Workspace)

Если у вас Google Workspace Organization:

1. **User Type** → **Internal**
2. Scopes доступны без верификации
3. Работает только для пользователей вашей организации

## 🔄 Временное решение (текущее):

Используем **только базовые scopes**:

```properties
# Без drive.readonly - работает для всех пользователей
spring.security.oauth2.client.registration.google.scope=openid,profile,email
```

### Что работает:
- ✅ Вход через Google
- ✅ Регистрация новых пользователей
- ✅ Получение email и имени
- ❌ Доступ к Google Drive (требует верификации)

## 📋 Checklist для верификации:

- [ ] Зарегистрировать домен (evalyze.by или другой)
- [ ] Создать Privacy Policy (политика конфиденциальности)
- [ ] Создать Terms of Service (условия использования)
- [ ] Загрузить логотип приложения
- [ ] Сделать скриншоты функционала
- [ ] Заполнить форму OAuth consent screen
- [ ] Отправить на верификацию
- [ ] Дождаться одобрения (3-7 дней)
- [ ] Обновить scopes в `application.properties`

## 🔧 Альтернативные API:

Если нужен доступ к файлам, но верификация еще не прошла:

### Вариант 1: Google Drive API (с ограничениями)
Используйте scope `drive.file` вместо `drive.readonly`:
- Доступ только к файлам, созданным приложением
- Не требует полной верификации

### Вариант 2: Google Picker API
- Пользователь выбирает файлы через UI
- Приложение получает только выбранные файлы
- Более безопасно, меньше требований

### Вариант 3: Загрузка файлов вручную
- Пользователь загружает файлы через форму
- Нет доступа к Google Drive
- Самый простой вариант

## ⚡ Быстрый старт (для тестирования):

### Если нужно тестировать drive.readonly СЕЙЧАС:

1. Добавьте себя как тестового пользователя в Google Cloud Console
2. Обновите `application.properties`:
   ```properties
   spring.security.oauth2.client.registration.google.scope=openid,profile,email,https://www.googleapis.com/auth/drive.readonly
   ```
3. Пересоберите и задеплойте
4. Войдите через Google с email, который добавили как тестового пользователя

## 📝 Итог:

**Текущая конфигурация (продакшен-ready):**
```properties
# Работает для ВСЕХ пользователей без ограничений
spring.security.oauth2.client.registration.google.scope=openid,profile,email
```

**После верификации (когда получим одобрение Google):**
```properties
# Работает для ВСЕХ + доступ к Google Drive
spring.security.oauth2.client.registration.google.scope=openid,profile,email,https://www.googleapis.com/auth/drive.readonly
```

**Для тестирования (только для тестовых пользователей):**
```properties
# Работает ТОЛЬКО для email, добавленных в Test Users
spring.security.oauth2.client.registration.google.scope=openid,profile,email,https://www.googleapis.com/auth/drive.readonly
```

## 🎯 Рекомендация:

Пока идет процесс верификации, используйте **базовые scopes** (`openid,profile,email`). 
Это позволит приложению работать для всех пользователей без ограничений.

После получения одобрения от Google, просто обновите scopes в конфигурации.


