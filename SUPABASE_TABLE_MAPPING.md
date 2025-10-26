# 🔄 Маппинг таблицы Supabase Users

## 📊 Структура существующей таблицы `users` в Supabase

```
id                      bigint (auto-increment, PRIMARY KEY)
created_at              timestamp with time zone
full_name               text
email                   text
telegram_chat_id        text
status                  text          ← ЗДЕСЬ храним role и company_id!
activation_code         text
access_token            text          ← ЗДЕСЬ храним google_oauth_token!
refresh_token           text
token_expires_at        timestamp with time zone
tracked_folders         jsonb
temp_selected_folders   jsonb
temp_full_folder_list   jsonb
Текст извечен           boolean
Изображения извлечены   boolean
Таблицы извлечены       boolean
Skills                  jsonb
last_updated            timestamp with time zone
```

## 🔧 Как мы адаптировали приложение

### 1. **SupabaseUserDto** (новый класс)
Точный маппинг с существующей таблицей Supabase

```java
@Data
public class SupabaseUserDto {
    private Long id;                    // bigint
    private String email;
    @JsonProperty("full_name")
    private String fullName;
    private String status;              // JSON: {"role":"EMPLOYEE","company_id":"..."}
    @JsonProperty("activation_code")
    private String activationCode;      // UUID, NOT NULL
    @JsonProperty("access_token")
    private String accessToken;         // OAuth токен
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;   // timestamp with time zone (важно!)
    @JsonProperty("last_updated")
    private OffsetDateTime lastUpdated; // timestamp with time zone
    // ... остальные поля
}
```

### 2. **User модель** (существующая)
Остается без изменений, используется внутри приложения

```java
public class User {
    private UUID id;
    private String email;
    private String fullName;
    private UserRole role;              // Извлекается из status
    private Company company;            // Извлекается из status
    private String googleOauthToken;    // Извлекается из access_token
}
```

### 3. **Маппинг при сохранении**

```java
// User → Supabase
Map<String, Object> userMap = new HashMap<>();
userMap.put("email", user.getEmail());
userMap.put("full_name", user.getFullName());

// activation_code - обязательное поле NOT NULL!
userMap.put("activation_code", UUID.randomUUID().toString());

// Сохраняем role и company_id в status как JSON
Map<String, Object> statusData = new HashMap<>();
statusData.put("role", user.getRole().name());
if (user.getCompany() != null) {
    statusData.put("company_id", user.getCompany().getId().toString());
}
userMap.put("status", objectMapper.writeValueAsString(statusData));

// OAuth токен сохраняем в access_token
userMap.put("access_token", user.getGoogleOauthToken());
```

### 4. **Маппинг при чтении**

```java
// Supabase → User
private User convertDtoToUser(SupabaseUserDto dto) {
    User user = new User();
    user.setEmail(dto.getEmail());
    user.setFullName(dto.getFullName());
    user.setGoogleOauthToken(dto.getAccessToken());
    
    // Парсим status JSON
    JsonNode statusJson = objectMapper.readTree(dto.getStatus());
    user.setRole(UserRole.valueOf(statusJson.get("role").asText()));
    // company_id можно извлечь при необходимости
    
    return user;
}
```

## ✅ Что изменилось

### Файл: `SupabaseUserRepository.java`
- ✅ Создан метод `convertDtoToUser()` для конвертации
- ✅ `save()` теперь **НЕ отправляет** `id` (auto-increment в Supabase)
- ✅ `save()` генерирует **`activation_code`** (UUID) - обязательное поле NOT NULL
- ✅ `save()` отправляет `status` как JSON строку с `role` и `company_id`
- ✅ `save()` отправляет OAuth токен в `access_token`
- ✅ `save()` возвращает `SupabaseUserDto`, конвертирует в `User`

### Новый файл: `SupabaseUserDto.java`
- ✅ DTO класс для точного маппинга с таблицей Supabase
- ✅ Использует `@JsonProperty` для snake_case полей

## 🧪 Тестирование

### 1. Пересоберите приложение:
```bash
./gradlew clean build -x test
```

### 2. Обновите JAR на сервере:
```bash
scp build/libs/NEW_NEW_MVP-0.0.1-SNAPSHOT.jar root@5.83.140.54:~/start.jar
```

### 3. Перезапустите:
```bash
ssh root@5.83.140.54
pkill -f start.jar
nohup java -jar start.jar > app.log 2>&1 &
tail -f app.log
```

### 4. Попробуйте зарегистрироваться

В логах должно быть:
```
=== SUPABASE INSERT DEBUG ===
Inserting user into Supabase: {
  email=test@example.com, 
  full_name=Эдвард Эдввардов, 
  status={"role":"EMPLOYEE"}, 
  access_token=null
}
Request body (as array): [{...}]
=== SUCCESS ===
User saved successfully: test@example.com
```

## 📝 Пример данных в Supabase

После успешной регистрации в таблице `users`:

```
id: 1
email: test@example.com
full_name: Эдвард Эдввардов
activation_code: 550e8400-e29b-41d4-a716-446655440000
status: {"role":"EMPLOYEE"}
access_token: null
created_at: 2025-10-23 12:34:56
... остальные поля: null
```

## ⚠️ Важно

1. **Не изменяйте структуру таблицы** - приложение адаптировано под существующую
2. **`id` генерируется автоматически** - не отправляем его в INSERT
3. **`activation_code` генерируется как UUID** - обязательное поле NOT NULL
4. **`status` хранит JSON** - можно добавлять туда дополнительные данные
5. **`access_token` используется** для OAuth токена
6. **Используем `OffsetDateTime`** для полей `timestamp with time zone` (не `LocalDateTime`!)
7. **RLS должна быть отключена** или настроены политики для INSERT

## 🔍 Отладка

Если ошибка `PGRST204`:
- ✅ Убедитесь, что отправляются **только существующие колонки**
- ✅ Проверьте snake_case имена: `full_name`, `access_token`
- ✅ Проверьте, что отправляется **массив**, а не одиночный объект

Если ошибка `403`:
- ✅ Отключите RLS: `ALTER TABLE users DISABLE ROW LEVEL SECURITY;`

Если ошибка `JSON decoding error: Cannot deserialize LocalDateTime`:
- ✅ Используйте `OffsetDateTime` вместо `LocalDateTime` для полей `timestamp with time zone`
- ✅ Supabase возвращает timestamps с timezone: `2025-10-23T19:11:57.627246+00:00`

Если ошибка `null value in column "activation_code"`:
- ✅ Добавьте генерацию `activation_code`: `userMap.put("activation_code", UUID.randomUUID().toString());`

