# 🚀 Руководство по миграции с Java Spring на Node.js

## 📋 Обзор миграции

Полная переписка бэкенда Evalyze с Java Spring Boot на Node.js с Express.js и Supabase JavaScript SDK.

## 🔄 Что было перенесено

### ✅ Структура проекта
- **Java Spring** → **Node.js Express**
- **JPA/Hibernate** → **Supabase JavaScript SDK**
- **Maven/Gradle** → **npm**
- **application.properties** → **.env**

### ✅ Модели данных
- `User.java` → `models/User.js`
- `Profile.java` → `models/Profile.js`
- `Company.java` → `models/Company.js`
- `JobRole.java` → `models/JobRole.js`
- `FileUpload.java` → `models/FileUpload.js`

### ✅ Репозитории
- `SupabaseUserRepository.java` → `repositories/UserRepository.js`
- `SupabaseProfileRepository.java` → `repositories/ProfileRepository.js`
- `SupabaseCompanyRepository.java` → `repositories/CompanyRepository.js`
- `JobRoleRepository.java` → `repositories/JobRoleRepository.js`
- `FileUploadRepository.java` → `repositories/FileUploadRepository.js`

### ✅ Сервисы
- `AuthService.java` → `services/AuthService.js`
- `ProfileService.java` → `services/ProfileService.js`
- `WebhookService.java` → `services/WebhookService.js`
- `FileUploadService.java` → `services/FileUploadService.js`

### ✅ Контроллеры
- `AuthController.java` → `controllers/AuthController.js`
- `ProfileController.java` → `controllers/ProfileController.js`
- `FileUploadController.java` → `controllers/FileController.js`
- `GoogleOAuthController.java` → интегрирован в `AuthController.js`

### ✅ Конфигурация
- `SecurityConfig.java` → `config/jwt.js`
- `SupabaseConfig.java` → `config/supabase.js`
- `application.properties` → `.env`

## 🔧 Ключевые изменения

### 1. Асинхронность
```java
// Java (блокирующий)
public User findById(UUID id) {
    return userRepository.findById(id).orElse(null);
}
```

```javascript
// Node.js (неблокирующий)
async findById(id) {
    const { data, error } = await supabase
        .from('users')
        .select('*')
        .eq('id', id)
        .single();
    return data ? User.fromSupabase(data) : null;
}
```

### 2. Обработка ошибок
```java
// Java
try {
    User user = userService.findById(id);
} catch (UserNotFoundException e) {
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
}
```

```javascript
// Node.js
try {
    const user = await userService.findById(id);
    if (!user) {
        return res.status(404).json({ error: 'User not found' });
    }
} catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: error.message });
}
```

### 3. JWT аутентификация
```java
// Java Spring Security
@PreAuthorize("hasRole('USER')")
public ResponseEntity<User> getCurrentUser() {
    // ...
}
```

```javascript
// Node.js middleware
router.get('/me', authenticateToken, authController.getCurrentUser);
```

### 4. Загрузка файлов
```java
// Java MultipartFile
@PostMapping("/upload")
public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files) {
    // ...
}
```

```javascript
// Node.js multer
const upload = multer({ storage: fileStorage });
router.post('/upload', authenticateToken, upload.array('files', 10), fileController.uploadFiles);
```

## 🚀 Запуск Node.js версии

### 1. Установка зависимостей
```bash
cd NODEJS
npm install
```

### 2. Настройка окружения
```bash
cp env.example .env
# Отредактируйте .env с вашими настройками Supabase
```

### 3. Запуск
```bash
# Разработка
npm run dev

# Продакшн
npm start

# Windows
start.bat

# Linux/Mac
./start.sh
```

## 🔗 API Endpoints

Все эндпоинты остались теми же:

### Аутентификация
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/login/activation-code`
- `GET /api/auth/google/callback`
- `GET /api/auth/me`

### Профили
- `POST /api/profile/`
- `GET /api/profile/me`
- `POST /api/profile/:profileId/generate-ai` (Кнопка 3)
- `POST /api/profile/:userId/assign-job-role` (Кнопка 2)

### Файлы
- `POST /api/files/upload`
- `GET /api/files/my-files`

### Вебхуки
- `POST /api/webhooks/analyze-competencies` (Кнопка 1)
- `POST /api/webhooks/assign-job-role`
- `POST /api/webhooks/generate-ai-profile`

## 🎯 Три основные кнопки

### Кнопка 1: Анализ компетенций
```javascript
// Отправка файлов и данных в n8n
await webhookService.sendCompetencyAnalysisWebhookWithFiles(
    userId, userEmail, userName, profileData, companyName, files
);
```

### Кнопка 2: Назначение роли
```javascript
// Обновление users.Role и отправка вебхука
await userRepository.updateUserRoleByEmail(email, jobRoleId);
await webhookService.sendJobRoleAssignmentWebhook(...);
```

### Кнопка 3: Генерация AI профиля
```javascript
// Сбор всех данных и отправка в n8n
await webhookService.sendAIProfileGenerationWebhook(
    userId, email, name, profileData, companyName, activationCode, telegramChatId, status
);
```

## 🗄 База данных

Структура БД осталась той же:
- `users` - пользователи
- `profiles` - профили
- `companies` - компании  
- `job_roles` - роли
- `file_uploads` - файлы

## 🔐 Безопасность

- JWT токены для аутентификации
- bcrypt для хеширования паролей
- Helmet для безопасности HTTP заголовков
- Rate limiting для защиты от DDoS
- CORS настройки

## 📊 Мониторинг

- Health check endpoints
- Логирование запросов (Morgan)
- Обработка ошибок
- Метрики памяти и CPU

## 🚀 Развертывание

### Docker
```bash
docker build -t evalyze-nodejs .
docker run -p 8089:8089 evalyze-nodejs
```

### PM2
```bash
pm2 start server.js --name evalyze-backend
```

### Docker Compose
```bash
docker-compose up -d
```

## ✅ Преимущества Node.js версии

1. **Производительность** - неблокирующий I/O
2. **Простота** - меньше кода, больше читаемости
3. **Скорость разработки** - быстрая итерация
4. **Масштабируемость** - легкое горизонтальное масштабирование
5. **Экосистема** - богатая экосистема npm пакетов

## 🔄 Обратная совместимость

- Все API endpoints остались теми же
- Структура JSON ответов не изменилась
- Вебхуки работают с теми же URL
- База данных не требует миграции

## 📞 Поддержка

При проблемах проверьте:
1. Переменные окружения в `.env`
2. Подключение к Supabase
3. Логи сервера
4. Статус вебхуков через `/api/health`

---

**Миграция завершена!** 🎉  
Node.js версия готова к использованию.
