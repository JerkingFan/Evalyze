# 🎯 ФИНАЛЬНОЕ ИСПРАВЛЕНИЕ - UUID vs BIGINT

## ❌ Проблема:
```
Cannot deserialize value of type `java.util.UUID` from String "150": 
UUID has to be represented by standard 36-char representation
```

## 🔍 Причина:
В таблице Supabase поле `id` имеет тип **bigint** (150, 151, ...), а не UUID!

Все методы в `SupabaseUserRepository`, которые использовали `SupabaseService.select("users", User.class, ...)`, пытались распарсить `id` как UUID → **ОШИБКА**!

## ✅ Решение:

### Все методы теперь используют **SupabaseUserDto** → **User**:

1. **`existsByEmail()`** ✅
   ```java
   // Было: select("users", User.class, ...)
   // Стало: select("users", SupabaseUserDto.class, ...)
   ```

2. **`findByEmail()`** ✅
   ```java
   return supabaseService.select("users", SupabaseUserDto.class, filters)
       .map(dtos -> dtos.isEmpty() ? Optional.empty() : 
            Optional.of(convertDtoToUser(dtos.get(0))));
   ```

3. **`findByRole()`, `findByCompanyId()`, `findByCompanyIdAndRole()`** ✅
   ```java
   return supabaseService.select("users", SupabaseUserDto.class, Map.of())
       .map(dtos -> dtos.stream()
           .map(this::convertDtoToUser)
           .filter(user -> user.getRole() == role)
           .toList());
   ```

4. **`findAll()`** ✅
   ```java
   return supabaseService.select("users", SupabaseUserDto.class)
       .map(dtos -> dtos.stream()
           .map(this::convertDtoToUser)
           .toList());
   ```

5. **`count()`, `countByRole()`, `countByCompanyId()`** ✅
   - Теперь вызывают соответствующие `find*()` методы

### Улучшен `convertDtoToUser()`:

```java
// Теперь извлекает BOTH role AND company_id из status JSON
if (statusJson.has("company_id")) {
    String companyIdStr = statusJson.get("company_id").asText();
    Company company = new Company();
    company.setId(UUID.fromString(companyIdStr));
    user.setCompany(company);
}
```

## 📦 Деплой:

```bash
# Windows
deploy.cmd

# Linux/Mac
./gradlew clean build -x test
scp build/libs/NEW_NEW_MVP-0.0.1-SNAPSHOT.jar root@5.83.140.54:~/start.jar
ssh root@5.83.140.54 "pkill -f start.jar && nohup java -jar start.jar > app.log 2>&1 &"
```

## 🎯 Ожидаемый результат:

### При регистрации:

1. **`existsByEmail()`** проверит email → **НЕТ ОШИБКИ UUID**
2. **`save()`** вставит пользователя → вернет `SupabaseUserDto`
3. **`convertDtoToUser()`** преобразует → `User` с правильным `id` и `role`
4. **Регистрация успешна!** ✅

### В логах:

```
SupabaseUserRepository.existsByEmail(233235@gmail.com)
User exists: false

=== SUPABASE INSERT DEBUG ===
Inserting user into Supabase: {email=233235@gmail.com, full_name=CUM, activation_code=..., status={"role":"EMPLOYEE"}}

Received SupabaseUserDto: id=150, email=233235@gmail.com

=== convertDtoToUser START ===
DTO: id=150, email=233235@gmail.com, status={"role":"EMPLOYEE"}
Parsing status JSON: {"role":"EMPLOYEE"}
Extracted role: EMPLOYEE
Converted ID: 150 -> a1b2c3d4-e5f6-...
=== convertDtoToUser END ===

=== SUCCESS ===
User saved successfully: 233235@gmail.com
User ID: a1b2c3d4-e5f6-...
User Role: EMPLOYEE

UserRepository.save() - SUCCESS: 233235@gmail.com
```

## 🔧 Что было исправлено:

| Метод | Было | Стало |
|-------|------|-------|
| `existsByEmail()` | `select(..., User.class, ...)` ❌ | `select(..., SupabaseUserDto.class, ...)` ✅ |
| `findByEmail()` | `select(..., User.class, ...)` ❌ | `select(..., SupabaseUserDto.class, ...) + convert` ✅ |
| `findByRole()` | `select(..., User.class, ...)` ❌ | `select(..., SupabaseUserDto.class, ...) + filter` ✅ |
| `findAll()` | `select(..., User.class)` ❌ | `select(..., SupabaseUserDto.class) + convert` ✅ |
| `convertDtoToUser()` | Только `role` | `role` + `company_id` ✅ |

## 🚀 ТЕПЕРЬ ДОЛЖНО РАБОТАТЬ 100%!

Все методы адаптированы под реальную структуру Supabase таблицы с **bigint id**!

