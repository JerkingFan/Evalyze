# Система входа по Activation Code

## Обзор

Реализована новая система аутентификации:
- **Компании** регистрируются через стандартную форму с email/password
- **Сотрудники** входят по уникальному `activation_code`, который генерируется компанией

## Backend

### Endpoints

#### 1. Регистрация компании
**POST** `/api/auth/register`

Только компании могут регистрироваться. Сотрудники создаются компанией.

**Request:**
```json
{
  "role": "COMPANY",
  "email": "company@example.com",
  "password": "password123",
  "fullName": "Company Name",
  "companyName": "My Company"
}
```

**Response:**
```json
{
  "token": "jwt_token",
  "email": "company@example.com",
  "role": "COMPANY",
  "fullName": "Company Name",
  "companyName": "My Company",
  "message": "Registration successful"
}
```

#### 2. Создание сотрудника (только для компаний)
**POST** `/api/auth/create-employee`

Требует аутентификации компании (JWT token в заголовке `Authorization: Bearer <token>`).

**Request:**
```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "telegramChatId": "@johndoe" // опционально
}
```

**Response:**
```json
{
  "email": "john@example.com",
  "role": "EMPLOYEE",
  "fullName": "John Doe",
  "companyName": "activation-code-here",
  "message": "Employee created successfully. Activation code: activation-code-here"
}
```

**Важно:** `companyName` в ответе содержит `activation_code`, который нужно передать сотруднику.

#### 3. Вход сотрудника по activation_code
**POST** `/api/auth/login-by-code`

Не требует аутентификации.

**Request:**
```json
{
  "activationCode": "activation-code-here"
}
```

**Response:**
```json
{
  "token": "jwt_token",
  "email": "john@example.com",
  "role": "EMPLOYEE",
  "fullName": "John Doe",
  "companyName": "My Company",
  "message": "Login successful"
}
```

### Модель User (Supabase)

```sql
create table public.users (
  id bigserial not null,
  created_at timestamp with time zone not null default now(),
  full_name text not null,
  email text not null,
  telegram_chat_id text null,
  status text not null default 'invited'::text,
  activation_code text not null,
  access_token text null,
  refresh_token text null,
  token_expires_at timestamp with time zone null,
  tracked_folders jsonb null,
  temp_selected_folders jsonb null,
  temp_full_folder_list jsonb null,
  "Текст извечен" boolean null,
  "Изображения извлечены" boolean null,
  "Таблицы извлечены" boolean null,
  "Skills" jsonb null,
  last_updated timestamp with time zone null,
  "Role" uuid null,
  password text null,
  constraint users_pkey primary key (id),
  constraint users_activation_code_key unique (activation_code),
  constraint users_email_key unique (email),
  constraint users_Role_fkey foreign KEY ("Role") references job_roles (id)
);
```

### Поля User

- `activation_code` - уникальный код для входа сотрудника (UUID)
- `status` - статус пользователя:
  - `invited` - сотрудник создан, но еще не вошел
  - `active` - сотрудник активировал аккаунт
- `password` - хэшированный пароль (только для компаний)
- `Role` (UUID) - ссылка на `job_roles.id`

## Frontend

### Модальное окно входа

Добавлены вкладки:
1. **Компания** - вход по email/password
2. **Сотрудник** - вход по activation_code

### Форма входа по activation_code

```html
<form id="activationCodeForm">
  <div class="mb-3">
    <label class="form-label">Код активации</label>
    <input type="text" class="form-control" id="activationCode" required 
           placeholder="Введите код активации от вашей компании">
    <small class="text-muted">Получите код активации у вашего HR-менеджера</small>
  </div>
  <button type="submit" class="btn btn-primary-evalyze w-100">Войти по коду</button>
</form>
```

### JavaScript обработчик

```javascript
async function handleActivationCodeLogin(e) {
    e.preventDefault();
    const activationCode = document.getElementById('activationCode').value;
    
    const response = await fetch('/api/auth/login-by-code', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ activationCode: activationCode })
    });
    
    const result = await response.json();
    
    if (response.ok) {
        localStorage.setItem('token', result.token);
        localStorage.setItem('userRole', result.role || 'EMPLOYEE');
        localStorage.setItem('userEmail', result.email);
        window.location.href = '/profile';
    }
}
```

## Процесс работы

### 1. Регистрация компании
1. Компания заполняет форму регистрации
2. Backend создает компанию и пользователя с ролью `COMPANY`
3. Генерируется уникальный `activation_code`
4. Компания получает JWT token и перенаправляется на `/company`

### 2. Создание сотрудника
1. Компания входит в систему
2. На странице `/company` создает профиль сотрудника
3. Backend генерирует уникальный `activation_code` для сотрудника
4. Компания получает `activation_code` и передает его сотруднику

### 3. Вход сотрудника
1. Сотрудник открывает главную страницу
2. Нажимает "Войти" → вкладка "Сотрудник"
3. Вводит `activation_code` от компании
4. Backend проверяет код, обновляет статус на `active`
5. Сотрудник получает JWT token и перенаправляется на `/profile`

## Безопасность

- `activation_code` - уникальный UUID, сложно угадать
- Пароли компаний хэшируются с помощью `PasswordEncoder`
- JWT токены для аутентификации
- Endpoint создания сотрудников доступен только компаниям

## Примечания

- Сотрудники НЕ могут регистрироваться самостоятельно
- Только компании создают сотрудников
- `activation_code` используется только один раз для первого входа
- После первого входа статус меняется с `invited` на `active`

