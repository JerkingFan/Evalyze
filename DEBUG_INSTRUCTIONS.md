# 🐛 Отладка ошибки "Error parsing JSON response"

## ✅ Что добавлено:

### 1. Детальное логирование в `SupabaseUserRepository`:
- Логирование полученного DTO от Supabase
- Логирование процесса конвертации DTO → User
- Детальные сообщения об ошибках с типами исключений

### 2. Детальное логирование в `UserRepository`:
- Логирование вызова `.block()`
- Отлов и логирование исключений

### 3. `@JsonIgnoreProperties(ignoreUnknown = true)` в `SupabaseUserDto`:
- Игнорирование неизвестных полей из Supabase
- Решает проблему с полями на кириллице (`Текст извечен`, `Изображения извлечены`, и т.д.)

### 4. Fallback для `status`:
- Если status пустой или null, устанавливается роль по умолчанию (EMPLOYEE)

## 🚀 Следующие шаги:

### 1. Пересоберите и обновите:
```bash
# Пересобрать
./gradlew clean build -x test

# Загрузить на сервер
scp build/libs/NEW_NEW_MVP-0.0.1-SNAPSHOT.jar root@5.83.140.54:~/start.jar

# Перезапустить
ssh root@5.83.140.54 "pkill -f start.jar && nohup java -jar start.jar > app.log 2>&1 &"
```

### 2. Попробуйте зарегистрироваться

### 3. Проверьте логи:
```bash
ssh root@5.83.140.54 "tail -100 app.log"
```

## 📊 Что искать в логах:

### При успехе:
```
=== SUPABASE INSERT DEBUG ===
Inserting user into Supabase: {email=..., full_name=..., activation_code=..., status=...}
Request body (as array): [{...}]

Received SupabaseUserDto: id=147, email=233235@gmail.com

=== convertDtoToUser START ===
DTO: id=147, email=233235@gmail.com, status={"role":"EMPLOYEE"}
Parsing status JSON: {"role":"EMPLOYEE"}
Extracted role: EMPLOYEE
Converted ID: 147 -> a1b2c3d4-...
=== convertDtoToUser END ===

=== SUCCESS ===
User saved successfully: 233235@gmail.com
User ID: a1b2c3d4-...
User Role: EMPLOYEE
===============

UserRepository.save() - SUCCESS: 233235@gmail.com
```

### При ошибке:
```
=== ERROR ===
Error type: com.fasterxml.jackson.databind.JsonMappingException
Error message: Cannot deserialize...
Full stack trace:
...
=============

UserRepository.save() - ERROR: ...
```

## 🔍 Возможные проблемы:

### 1. Проблема с парсингом JSON:
- **Причина:** Jackson не может распарсить ответ от Supabase
- **Решение:** `@JsonIgnoreProperties(ignoreUnknown = true)` должно помочь

### 2. Пустой ответ от Supabase:
- **Причина:** Supabase возвращает пустой массив `[]`
- **Решение:** Проверьте RLS политики, возможно INSERT прошел, но SELECT заблокирован

### 3. Неправильный формат данных:
- **Причина:** Поля с кириллицей или jsonb полями
- **Решение:** Уже исправлено с `@JsonIgnoreProperties`

## 🎯 После исправления:

Если логи показывают успех, но фронтенд все равно получает ошибку, проблема может быть в:
1. JWT токене (проверьте генерацию токена)
2. Формате ответа `AuthResponse`
3. CORS настройках

Пришлите полные логи, если проблема сохранится!

