# 📋 Функциональность личного кабинета пользователя

## 🎯 Задача
Реализовать три кнопки в личном кабинете пользователя с указанным функционалом.

## ⚠️ Важно: Работа с Supabase
Проект использует базу данных **Supabase** с таблицей `job_roles` вместо `company_content`. Все данные хранятся в Supabase.

## 🔗 Webhook Integration
Проект интегрирован с n8n через три webhook-эндпоинта:
- **Кнопка 1**: `https://guglovskij.app.n8n.cloud/webhook/0d0a654b-772e-447a-9223-8b443f788175`
- **Кнопка 2**: `https://guglovskij.app.n8n.cloud/webhook/113447c6-c39e-410c-ab15-4f5ab7809fd9`
- **Кнопка 3**: `https://guglovskij.app.n8n.cloud/webhook/bbd2959f-bedc-43fc-a558-69c04fe7b4db`

---

## ✅ Реализованный функционал

### **Кнопка 1: Загрузить и анализировать компетенции**
**Что делает:**
- Пользователь загружает свои файлы (резюме, портфолио, сертификаты)
- Файлы отправляются на анализ
- Информация из файлов и данные из БД отправляются в n8n через webhook

**Реализация:**
- Использует существующую модалку `employeeUploadModal`
- Функция `showUploadAndAnalyzeModal()` открывает модальное окно
- После загрузки файлов они отправляются в n8n для анализа
- **Webhook**: После успешной загрузки файлов автоматически отправляется webhook в n8n с данными пользователя (userId, email, profileData, companyName)
- **Webhook URL**: `https://guglovskij.app.n8n.cloud/webhook/0d0a654b-772e-447a-9223-8b443f788175`

---

### **Кнопка 2: Выбрать роль из списка**
**Что делает:**
- Пользователь видит список всех доступных ролей из таблицы `job_roles` (company_content с content_type = JOB_ROLE)
- После выбора роли она присваивается пользователю
- Выбранная роль сохраняется в профиле пользователя

**Реализация:**
- API endpoint: `GET /api/job-roles` - получить все роли
- API endpoint: `POST /api/profiles/assign-role/{userId}` - назначить роль
- Функция `showJobRolesModal()` открывает модальное окно с выбором ролей
- Функция `selectJobRole()` назначает выбранную роль пользователю
- Модальное окно `jobRolesModal` показывает список доступных ролей

**Backend:**
- `JobRoleController.java` - контроллер для работы с job_roles
- `ProfileService.assignJobRoleToUser()` - метод назначения роли
- Роль сохраняется в `profile.profile_data` как JSON
- Используется таблица Supabase `job_roles` вместо `company_content`
- **Webhook**: После назначения роли автоматически отправляется webhook в n8n с информацией о назначенной роли (jobRoleId, jobRoleTitle, jobRoleDescription)
- **Webhook URL**: `https://guglovskij.app.n8n.cloud/webhook/113447c6-c39e-410c-ab15-4f5ab7809fd9`

---

### **Кнопка 3: Создать мой AI-профиль**
**Что делает:**
- На основе информации из БД отправляет запрос в n8n
- N8n создает AI-профиль пользователя
- Профиль будет содержать рекомендации и анализ

**Реализация:**
- API endpoint: `POST /api/profiles/generate-ai/{profileId}` - генерация AI-профиля
- Функция `buildAIProfile()` отправляет webhook в n8n
- Webhook содержит: userId, email, имя, данные профиля, название компании
- **Webhook URL**: `https://guglovskij.app.n8n.cloud/webhook/bbd2959f-bedc-43fc-a558-69c04fe7b4db`

---

## 🔧 Технические детали

### **Новые файлы:**
1. `src/main/java/org/example/new_new_mvp/model/JobRole.java` - модель для job_roles
2. `src/main/java/org/example/new_new_mvp/dto/JobRoleDto.java` - DTO для job_roles
3. `src/main/java/org/example/new_new_mvp/repository/JobRoleRepository.java` - репозиторий для job_roles
4. `src/main/java/org/example/new_new_mvp/repository/JobRoleRepositoryAdapter.java` - адаптер-репозиторий
5. `src/main/java/org/example/new_new_mvp/controller/JobRoleController.java` - контроллер для job_roles (переименован из CompanyContentController)
6. Модальное окно `jobRolesModal` в `profile.html`
7. JavaScript функции в `profile.js`

### **Измененные файлы:**
1. `src/main/java/org/example/new_new_mvp/service/WebhookService.java` - добавлены три метода для отправки webhook'ов (sendCompetencyAnalysisWebhook, sendJobRoleAssignmentWebhook, sendAIProfileGenerationWebhook)
2. `src/main/java/org/example/new_new_mvp/service/ProfileService.java` - добавлен метод `assignJobRoleToUser()` с интеграцией webhook'а
3. `src/main/java/org/example/new_new_mvp/service/ProfileService.java` - метод `generateAIProfile()` обновлён для использования нового webhook'а
4. `src/main/java/org/example/new_new_mvp/controller/FileUploadController.java` - добавлена отправка webhook'а после загрузки файлов
5. `src/main/java/org/example/new_new_mvp/controller/ProfileController.java` - добавлен endpoint `assignJobRole()`
6. `src/main/resources/templates/profile.html` - добавлены три кнопки и модальное окно
7. `src/main/resources/static/js/profile.js` - добавлены функции для работы с кнопками

### **API Endpoints:**
```
GET  /api/job-roles - получить все роли
GET  /api/job-roles/by-company/{companyId} - получить роли компании
GET  /api/job-roles/by-type/{roleType} - получить роли по типу
GET  /api/job-roles/{id} - получить роль по ID
POST /api/profiles/assign-role/{userId} - назначить роль пользователю
POST /api/profiles/generate-ai/{profileId} - создать AI-профиль
```

---

## 📝 Структура данных

### **Таблица job_roles (Supabase):**
```sql
CREATE TABLE job_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID REFERENCES companies(id) ON DELETE CASCADE,
    role_type TEXT NOT NULL CHECK (role_type IN ('ROLE', 'VACANCY', 'TEMPLATE')),
    title TEXT NOT NULL,
    description TEXT,
    requirements JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

### **Профиль пользователя:**
При выборе роли данные сохраняются в `profile.profile_data`:
```json
{
    "currentPosition": "Senior Python Developer",
    "jobRoleData": {...},
    "assignedRoleId": "uuid-of-role"
}
```

---

## 🚀 Использование

### **Для пользователя:**
1. Войдите в личный кабинет
2. Найдите свой профиль по email
3. Используйте три кнопки для:
   - Загрузки и анализа файлов
   - Выбора роли из списка
   - Создания AI-профиля

### **Для администратора:**
Добавьте роли в таблицу `job_roles`:
```sql
INSERT INTO job_roles (company_id, role_type, title, description, requirements)
VALUES (
    'company-uuid',
    'ROLE',
    'Senior Python Developer',
    'Senior Python Developer position',
    '{"skills": ["Python", "Django"], "experience": "5+ years", "salary": "100000"}'::jsonb
);
```

---

## 📨 Структура Webhook Сообщений

### **Кнопка 1: Анализ компетенций**
```json
{
    "action": "analyze_competencies",
    "userId": "uuid",
    "userEmail": "user@example.com",
    "userName": "John Doe",
    "profileData": {...},
    "companyName": "Company Name",
    "timestamp": "2024-01-01T12:00:00"
}
```

### **Кнопка 2: Назначение роли**
```json
{
    "action": "assign_job_role",
    "userId": "uuid",
    "userEmail": "user@example.com",
    "userName": "John Doe",
    "jobRoleId": "uuid",
    "jobRoleTitle": "Senior Python Developer",
    "jobRoleDescription": "Senior Python Developer position",
    "profileData": {...},
    "companyName": "Company Name",
    "timestamp": "2024-01-01T12:00:00"
}
```

### **Кнопка 3: Генерация AI-профиля**
```json
{
    "action": "generate_ai_profile",
    "userId": "uuid",
    "userEmail": "user@example.com",
    "userName": "John Doe",
    "profileData": {...},
    "companyName": "Company Name",
    "timestamp": "2024-01-01T12:00:00"
}
```

---

## 📦 Зависимости
- Bootstrap 5.3.0 (для модальных окон)
- Font Awesome 6.0 (для иконок)
- Bootstrap Icons

---

## ✨ Дополнительные возможности
- Валидация входных данных
- Обработка ошибок
- Визуальная обратная связь (spinner, alerts)
- Автоматическое обновление страницы после назначения роли
- Экранирование HTML для безопасности (XSS защита)
- Интеграция с n8n через webhook'и
- Автоматическая отправка данных в n8n после действий пользователя
