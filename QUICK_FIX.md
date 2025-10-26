# 🚀 БЫСТРОЕ ИСПРАВЛЕНИЕ

## ✅ Исправлено в этом коммите:

### 1. ❌ Ошибка: `null value in column "activation_code"`
**Решение:** Добавлена генерация UUID для `activation_code`
```java
userMap.put("activation_code", UUID.randomUUID().toString());
```

### 2. ❌ Ошибка: `Cannot deserialize LocalDateTime from String with timezone`
**Решение:** Изменен тип с `LocalDateTime` на `OffsetDateTime`
```java
// Было:
private LocalDateTime createdAt;

// Стало:
private OffsetDateTime createdAt;
```

### 3. ❌ Ошибка: `Error parsing JSON response`
**Решение:** Добавлен `@JsonIgnoreProperties(ignoreUnknown = true)` + детальное логирование
```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupabaseUserDto {
    // Теперь игнорирует неизвестные поля (кириллица, и т.д.)
}
```

## 📦 Быстрый деплой:

### Вариант 1: Используйте готовый скрипт

**Windows:**
```cmd
deploy.cmd
```

**Linux/Mac:**
```bash
chmod +x deploy.sh
./deploy.sh
```

### Вариант 2: Вручную

```bash
# 1. Пересобрать
./gradlew clean build -x test

# 2. Загрузить на сервер
scp build/libs/NEW_NEW_MVP-0.0.1-SNAPSHOT.jar root@5.83.140.54:~/start.jar

# 3. Перезапустить
ssh root@5.83.140.54 "pkill -f start.jar && nohup java -jar start.jar > app.log 2>&1 &"

# 4. ГОТОВО! Регистрация должна работать!
```

## 📝 Что отправляется в Supabase:

```json
{
  "email": "233235@gmail.com",
  "full_name": "CUM",
  "activation_code": "550e8400-e29b-41d4-a716-446655440000",
  "status": "{\"role\":\"EMPLOYEE\"}"
}
```

## ✅ Что возвращается:

```json
{
  "id": 147,
  "email": "233235@gmail.com",
  "full_name": "CUM",
  "activation_code": "550e8400-e29b-41d4-a716-446655440000",
  "status": "{\"role\":\"EMPLOYEE\"}",
  "created_at": "2025-10-23T19:11:57.627246+00:00"
}
```

## 🎯 Итоговый статус:

- ✅ Маппинг с существующей таблицей Supabase
- ✅ Генерация `activation_code` (UUID)
- ✅ Правильный парсинг `timestamp with time zone` → `OffsetDateTime`
- ✅ Сохранение `role` в поле `status` как JSON
- ✅ Отправка данных как массив для PostgREST
- ✅ Детальное логирование ошибок

## 🔥 ТЕПЕРЬ ДОЛЖНО РАБОТАТЬ!

