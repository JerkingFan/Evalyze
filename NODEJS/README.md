# Evalyze Node.js Backend

Полная переписка бэкенда с Java Spring на Node.js с Express и Supabase.

## 🚀 Особенности

- **Express.js** - быстрый и гибкий веб-фреймворк
- **Supabase** - PostgreSQL база данных с JavaScript SDK
- **JWT аутентификация** - безопасная авторизация
- **Webhook интеграция** - интеграция с n8n для автоматизации
- **Загрузка файлов** - поддержка множественной загрузки файлов
- **RESTful API** - стандартные HTTP методы и статус коды

## 📁 Структура проекта

```
NODEJS/
├── config/           # Конфигурация (Supabase, JWT, БД)
├── controllers/      # Контроллеры для обработки запросов
├── models/          # Модели данных
├── repositories/    # Репозитории для работы с БД
├── routes/          # Маршруты API
├── services/        # Бизнес-логика
├── server.js        # Главный файл сервера
├── package.json     # Зависимости и скрипты
└── env.example      # Пример переменных окружения
```

## 🛠 Установка и запуск

### 1. Установка зависимостей
```bash
cd NODEJS
npm install
```

### 2. Настройка переменных окружения
```bash
cp env.example .env
# Отредактируйте .env файл с вашими настройками
```

### 3. Запуск в режиме разработки
```bash
npm run dev
```

### 4. Запуск в продакшене
```bash
npm start
```

## 🔧 Конфигурация

### Переменные окружения (.env)

```env
# Server Configuration
PORT=8089
NODE_ENV=development

# Supabase Configuration
SUPABASE_URL=your_supabase_url
SUPABASE_ANON_KEY=your_supabase_anon_key
SUPABASE_SERVICE_ROLE_KEY=your_supabase_service_role_key

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_here
JWT_EXPIRES_IN=24h

# Google OAuth Configuration
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GOOGLE_REDIRECT_URI=http://localhost:8089/api/auth/google/callback

# n8n Webhook URLs
WEBHOOK_ANALYZE_COMPETENCIES=https://guglovskij.app.n8n.cloud/webhook/0d0a654b-772e-447a-9223-8b443f788175
WEBHOOK_ASSIGN_JOB_ROLE=https://guglovskij.app.n8n.cloud/webhook/113447c6-c39e-410c-ab15-4f5ab7809fd9
WEBHOOK_GENERATE_AI_PROFILE=https://guglovskij.app.n8n.cloud/webhook/bbd2959f-bedc-43fc-a558-69c0fe7b4db

# File Upload Configuration
UPLOAD_DIR=uploads
MAX_FILE_SIZE=10485760

# CORS Configuration
CORS_ORIGIN=http://localhost:3000
```

## 📚 API Endpoints

### Аутентификация (`/api/auth`)
- `POST /register` - Регистрация пользователя
- `POST /login` - Вход по email/паролю
- `POST /login/activation-code` - Вход по коду активации
- `GET /google/callback` - Google OAuth callback
- `GET /me` - Получить текущего пользователя
- `PUT /me` - Обновить данные пользователя

### Профили (`/api/profile`)
- `POST /` - Создать/обновить профиль
- `GET /me` - Получить свой профиль
- `GET /user/:userId` - Получить профиль пользователя
- `POST /:profileId/generate-ai` - Генерация AI профиля (Кнопка 3)
- `POST /:userId/assign-job-role` - Назначить роль (Кнопка 2)
- `POST /employee` - Создать профиль сотрудника

### Файлы (`/api/files`)
- `POST /upload` - Загрузить файлы
- `GET /my-files` - Получить свои файлы
- `GET /storage-stats` - Статистика хранилища
- `DELETE /:fileId` - Удалить файл

### Вебхуки (`/api/webhooks`)
- `GET /config` - Конфигурация вебхуков
- `POST /test-all` - Тест всех вебхуков
- `POST /analyze-competencies` - Анализ компетенций (Кнопка 1)
- `POST /assign-job-role` - Назначение роли
- `POST /generate-ai-profile` - Генерация AI профиля

### Компании (`/api/company`)
- `GET /` - Получить все компании
- `POST /` - Создать компанию
- `GET /:id` - Получить компанию
- `PUT /:id` - Обновить компанию
- `DELETE /:id` - Удалить компанию
- `GET /:id/job-roles` - Роли компании

### Здоровье системы (`/api/health`)
- `GET /` - Общий статус
- `GET /database` - Статус БД
- `GET /system` - Информация о системе

## 🔐 Аутентификация

API использует JWT токены для аутентификации. Добавьте заголовок:
```
Authorization: Bearer <your_jwt_token>
```

## 📊 Три основные кнопки

### Кнопка 1: Анализ компетенций
- Загрузка файлов пользователя
- Отправка данных в n8n для анализа
- Webhook: `WEBHOOK_ANALYZE_COMPETENCIES`

### Кнопка 2: Назначение роли
- Выбор роли из списка job_roles
- Обновление поля `users.Role`
- Webhook: `WEBHOOK_ASSIGN_JOB_ROLE`

### Кнопка 3: Генерация AI профиля
- Сбор всех данных пользователя
- Отправка в n8n для генерации
- Webhook: `WEBHOOK_GENERATE_AI_PROFILE`

## 🗄 База данных

Проект использует Supabase (PostgreSQL) с таблицами:
- `users` - пользователи
- `profiles` - профили пользователей
- `companies` - компании
- `job_roles` - роли в компании
- `file_uploads` - загруженные файлы

## 🚀 Развертывание

### Локально
```bash
npm install
npm run dev
```

### Docker
```bash
docker build -t evalyze-nodejs .
docker run -p 8089:8089 evalyze-nodejs
```

### PM2
```bash
npm install -g pm2
pm2 start server.js --name evalyze-backend
```

## 🧪 Тестирование

```bash
# Запуск тестов
npm test

# Тест API endpoints
curl http://localhost:8089/api/health
```

## 📝 Логирование

Сервер использует Morgan для логирования HTTP запросов и встроенный console.log для отладки.

## 🔧 Отладка

Для отладки установите `NODE_ENV=development` в .env файле.

## 📞 Поддержка

При возникновении проблем проверьте:
1. Переменные окружения
2. Подключение к Supabase
3. Логи сервера
4. Статус вебхуков

---

**Версия:** 1.0.0  
**Автор:** Evalyze Team  
**Лицензия:** MIT
