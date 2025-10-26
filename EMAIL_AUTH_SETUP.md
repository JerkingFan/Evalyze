# Настройка Email Авторизации

## Описание
Реализована авторизация через подтверждение email с отправкой кода. Пользователь вводит email, получает код на почту, вводит код и авторизуется.

## Что реализовано

### Backend
1. **Модель EmailVerification** - хранение кодов подтверждения
2. **EmailService** - отправка email с кодами
3. **EmailAuthService** - бизнес-логика авторизации
4. **EmailAuthController** - REST API для фронтенда
5. **EmailVerificationRepository** - работа с базой данных

### Frontend
1. **Обновленные модальные окна** - убрана Google OAuth, добавлены поля для email и кода
2. **JavaScript логика** - пошаговая авторизация (email → код → вход)
3. **Обработка ошибок** - показ сообщений пользователю

### База данных
1. **Таблица email_verifications** - хранение кодов
2. **Индексы** - для быстрого поиска
3. **Очистка** - автоматическое удаление истекших кодов

## Настройка

### 1. Настройка Email (Gmail)
```properties
# В application.properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Email настройки
app.email.from=your-email@gmail.com
app.email.subject=Код подтверждения - Evalyze
```

### 2. Переменные окружения
```bash
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
```

### 3. Gmail App Password
1. Включите 2FA в Google аккаунте
2. Создайте App Password для приложения
3. Используйте App Password вместо обычного пароля

## API Endpoints

### Отправка кода
```
POST /api/auth/email/send-code
Content-Type: application/json

{
  "email": "user@example.com"
}
```

### Верификация кода
```
POST /api/auth/email/verify
Content-Type: application/json

{
  "email": "user@example.com",
  "verificationCode": "123456"
}
```

## Безопасность

1. **Коды действительны 10 минут**
2. **Максимум 3 попытки ввода**
3. **Автоматическая очистка истекших кодов**
4. **Один активный код на email**

## Логика работы

1. Пользователь вводит email
2. Система генерирует 6-значный код
3. Код отправляется на email
4. Пользователь вводит код
5. Система проверяет код и создает JWT токен
6. Пользователь авторизован

## Тестирование

1. Запустите приложение
2. Откройте главную страницу
3. Нажмите "Войти"
4. Введите email
5. Проверьте почту
6. Введите код
7. Должен произойти вход

## Troubleshooting

### Email не отправляется
- Проверьте настройки SMTP
- Убедитесь, что используется App Password
- Проверьте логи приложения

### Код не принимается
- Проверьте, что код не истек
- Убедитесь, что не превышено количество попыток
- Проверьте логи базы данных

### Ошибки базы данных
- Убедитесь, что таблица email_verifications создана
- Проверьте права доступа к базе данных
- Запустите миграции схемы
