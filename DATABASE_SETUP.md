# Настройка базы данных для Evalyze

## Установка MySQL

### Windows:
1. Скачайте MySQL Community Server с официального сайта
2. Установите с настройками по умолчанию
3. Запомните пароль root пользователя

### macOS:
```bash
brew install mysql
brew services start mysql
```

### Ubuntu/Debian:
```bash
sudo apt update
sudo apt install mysql-server
sudo mysql_secure_installation
```

## Создание базы данных

1. Подключитесь к MySQL:
```bash
mysql -u root -p
```

2. Создайте базу данных:
```sql
CREATE DATABASE evalyze CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. Создайте пользователя (опционально):
```sql
CREATE USER 'evalyze_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON evalyze.* TO 'evalyze_user'@'localhost';
FLUSH PRIVILEGES;
```

## Настройка приложения

Обновите файл `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/evalyze?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

## Структура таблиц

Приложение автоматически создаст следующие таблицы:

### companies
- `id` (CHAR(36), UUID) - Уникальный идентификатор компании
- `name` (VARCHAR(255)) - Название компании
- `created_at` (TIMESTAMP) - Дата создания

### users
- `id` (CHAR(36), UUID) - Уникальный идентификатор пользователя
- `company_id` (CHAR(36)) - Ссылка на компанию
- `email` (VARCHAR(255), UNIQUE) - Email пользователя
- `full_name` (VARCHAR(255)) - Полное имя
- `role` (ENUM: EMPLOYEE, ADMIN) - Роль пользователя
- `google_oauth_token` (JSON) - Токен OAuth

### invitations
- `id` (CHAR(36), UUID) - Уникальный идентификатор приглашения
- `company_id` (CHAR(36)) - Ссылка на компанию
- `email` (VARCHAR(255)) - Email приглашенного
- `invitation_code` (VARCHAR(255), UNIQUE) - Код приглашения
- `status` (ENUM: PENDING, ACCEPTED) - Статус приглашения
- `expires_at` (TIMESTAMP) - Дата истечения

### profiles
- `user_id` (CHAR(36)) - Ссылка на пользователя
- `profile_data` (JSON) - Данные профиля
- `status` (ENUM: PENDING, PROCESSING, COMPLETED) - Статус профиля
- `last_updated` (TIMESTAMP) - Последнее обновление

### profile_snapshots
- `id` (CHAR(36), UUID) - Уникальный идентификатор снимка
- `user_id` (CHAR(36)) - Ссылка на пользователя
- `snapshot_date` (TIMESTAMP) - Дата снимка
- `profile_data` (JSON) - Данные профиля на момент снимка

### company_content
- `id` (CHAR(36), UUID) - Уникальный идентификатор контента
- `company_id` (CHAR(36)) - Ссылка на компанию
- `content_type` (ENUM: JOB_ROLE, INTERNAL_VACANCY, COURSE) - Тип контента
- `title` (VARCHAR(255)) - Заголовок
- `data` (JSON) - Данные контента

## Запуск приложения

1. Убедитесь, что MySQL запущен
2. Запустите приложение:
```bash
./gradlew bootRun
```

3. Приложение автоматически:
   - Создаст базу данных если её нет
   - Выполнит SQL скрипты для создания таблиц
   - Вставит тестовые данные

## Проверка подключения

После запуска приложения проверьте подключение:

1. Откройте браузер: `http://localhost:8089/h2-console`
2. Или подключитесь к MySQL напрямую:
```bash
mysql -u root -p evalyze
```

## Тестовые данные

Приложение автоматически создаст:

### Компании:
- Компания А
- Компания Б  
- Компания В

### Пользователи:
- user1@company_a.com (EMPLOYEE)
- user2@company_a.com (ADMIN)
- user3@company_b.com (EMPLOYEE)

### Приглашения:
- INVITE123 для invitee1@company_a.com
- INVITE456 для invitee2@company_b.com

## API Endpoints

После запуска доступны следующие эндпоинты:

### Companies:
- `GET /api/companies` - Получить все компании
- `GET /api/companies/{id}` - Получить компанию по ID
- `POST /api/companies` - Создать компанию (только ADMIN)
- `PUT /api/companies/{id}` - Обновить компанию (только ADMIN)
- `DELETE /api/companies/{id}` - Удалить компанию (только ADMIN)

### Invitations:
- `POST /api/invitations` - Создать приглашение (только ADMIN)
- `GET /api/invitations/company/{companyId}` - Получить приглашения компании
- `GET /api/invitations/code/{code}` - Получить приглашение по коду
- `POST /api/invitations/accept` - Принять приглашение
- `DELETE /api/invitations/{id}` - Удалить приглашение (только ADMIN)

### Profiles:
- `POST /api/profiles/user/{userId}` - Создать/обновить профиль
- `GET /api/profiles/user/{userId}` - Получить профиль пользователя
- `GET /api/profiles/company/{companyId}` - Получить профили компании
- `PUT /api/profiles/user/{userId}/status/{status}` - Обновить статус профиля

## Troubleshooting

### Ошибка подключения к MySQL:
1. Убедитесь, что MySQL запущен
2. Проверьте логин/пароль в application.properties
3. Проверьте, что порт 3306 доступен

### Ошибка создания таблиц:
1. Убедитесь, что у пользователя есть права на создание таблиц
2. Проверьте, что база данных создана
3. Проверьте логи приложения

### Ошибка вставки данных:
1. Проверьте, что SQL скрипты корректны
2. Убедитесь, что нет дублирующихся данных
3. Проверьте foreign key constraints
