# Настройка Email для отправки кодов подтверждения

## Проблема
Для отправки кодов подтверждения нужен настроенный SMTP сервер. В приложении используется Gmail SMTP.

## Решение

### 1. Создайте Gmail аккаунт (если нет)
- Перейдите на https://gmail.com
- Создайте новый аккаунт или используйте существующий

### 2. Включите 2FA (двухфакторная аутентификация)
1. Войдите в Gmail
2. Перейдите в **Настройки** → **Безопасность**
3. Включите **2-Step Verification**

### 3. Создайте App Password
1. В настройках безопасности найдите **App passwords**
2. Выберите **Mail** и **Other (Custom name)**
3. Введите название: "Evalyze App"
4. Скопируйте сгенерированный пароль (16 символов)

### 4. Настройте переменные окружения

#### Windows (PowerShell):
```powershell
$env:MAIL_USERNAME="your-email@gmail.com"
$env:MAIL_PASSWORD="your-16-character-app-password"
```

#### Windows (CMD):
```cmd
set MAIL_USERNAME=your-email@gmail.com
set MAIL_PASSWORD=your-16-character-app-password
```

#### Linux/Mac:
```bash
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-16-character-app-password"
```

### 5. Или создайте файл .env
Создайте файл `.env` в корне проекта:
```
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-16-character-app-password
```

### 6. Проверьте настройки в application.properties
Убедитесь, что в `src/main/resources/application.properties` есть:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME:your-email@gmail.com}
spring.mail.password=${MAIL_PASSWORD:your-app-password}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## Тестирование

### 1. Запустите приложение
```bash
./gradlew bootRun
```

### 2. Откройте браузер
- Перейдите на http://localhost:8089
- Нажмите "Войти"
- Введите email
- Нажмите "Отправить код"

### 3. Проверьте почту
- Откройте Gmail
- Найдите письмо с кодом подтверждения
- Введите код в форму

## Troubleshooting

### Ошибка "Authentication failed"
- Проверьте, что 2FA включена
- Убедитесь, что используете App Password, а не обычный пароль
- Проверьте правильность переменных окружения

### Ошибка "Connection refused"
- Проверьте интернет соединение
- Убедитесь, что порт 587 не заблокирован
- Попробуйте использовать порт 465 с SSL

### Письма не приходят
- Проверьте папку "Спам"
- Убедитесь, что email адрес правильный
- Проверьте логи приложения

## Альтернативные SMTP провайдеры

Если Gmail не работает, можно использовать:

### Mailgun
```properties
spring.mail.host=smtp.mailgun.org
spring.mail.port=587
spring.mail.username=your-mailgun-username
spring.mail.password=your-mailgun-password
```

### SendGrid
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=your-sendgrid-api-key
```

### Yandex
```properties
spring.mail.host=smtp.yandex.ru
spring.mail.port=587
spring.mail.username=your-yandex-email
spring.mail.password=your-yandex-password
```

## Безопасность

1. **Никогда не коммитьте** реальные пароли в Git
2. **Используйте переменные окружения** для чувствительных данных
3. **Регулярно обновляйте** App Passwords
4. **Мониторьте** логи на предмет подозрительной активности

## Логи

Для отладки включите логирование в `application.properties`:
```properties
logging.level.org.springframework.mail=DEBUG
logging.level.com.sun.mail=DEBUG
```

Это покажет детальную информацию о процессе отправки email.
