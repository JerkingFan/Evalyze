# Supabase Configuration Guide

## ✅ Настройка завершена

Ваше приложение Spring Boot теперь полностью настроено для работы с Supabase PostgreSQL.

### 🔧 Что было настроено:

1. **База данных**: Прямое подключение к Supabase PostgreSQL
2. **JPA**: Настроены JPA репозитории для работы с базой данных
3. **Модели**: Добавлены JPA аннотации к моделям
4. **Конфигурация**: Создана гибкая система конфигурации

### 🌐 Тестовые эндпоинты:

#### Проверка подключения к базе данных:
- `GET /api/database/test-connection` - Тест подключения к PostgreSQL
- `GET /api/database/connection-info` - Информация о подключении

#### Тестирование JPA репозиториев:
- `GET /api/jpa-test/test` - Тест JPA подключения
- `GET /api/jpa-test/users` - Получить всех пользователей
- `GET /api/jpa-test/companies` - Получить все компании
- `POST /api/jpa-test/company` - Создать компанию
- `POST /api/jpa-test/user` - Создать пользователя

### 📊 Параметры подключения:

- **JDBC URL**: `jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:6543/postgres?sslmode=require`
- **Host**: `aws-1-us-east-1.pooler.supabase.com`
- **Port**: `6543` (Connection Pooler)
- **Database**: `postgres`
- **Username**: `postgres.fqyklholxklhwydksazc`
- **Password**: `2455767877_start`
- **SSL Mode**: `require` (обязательно для Supabase)

### ⚙️ HikariCP Connection Pool:

- **Maximum Pool Size**: 10
- **Minimum Idle**: 1
- **Idle Timeout**: 600000ms (10 минут)
- **Connection Timeout**: 30000ms (30 секунд)

### 🚀 Как запустить:

1. Запустите приложение: `./gradlew bootRun`
2. Откройте браузер: `http://localhost:8089`
3. Проверьте подключение: `http://localhost:8089/api/database/test-connection`

### 🔄 Переключение между режимами:

В `application.properties` можно изменить:
```properties
app.repository.type=jpa  # Использовать JPA репозитории
app.repository.type=supabase  # Использовать Supabase API
```

### 📝 Важные файлы:

- `src/main/resources/application.properties` - Основная конфигурация
- `src/main/java/org/example/new_new_mvp/config/DatabaseConfig.java` - Конфигурация БД
- `src/main/java/org/example/new_new_mvp/config/JpaConfig.java` - JPA конфигурация
- `src/main/java/org/example/new_new_mvp/repository/Jpa*Repository.java` - JPA репозитории

Приложение готово к работе с Supabase! 🎉
