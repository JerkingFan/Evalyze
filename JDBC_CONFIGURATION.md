# Конфигурация JDBC для Supabase PostgreSQL

## ✅ Текущая конфигурация

### 📝 application.properties

```properties
# Database Configuration for Supabase PostgreSQL (Direct Connection via Pooler)
spring.datasource.url=jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:6543/postgres?sslmode=require
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=postgres.fqyklholxklhwydksazc
spring.datasource.password=2455767877_start

# HikariCP Connection Pool Settings (optimized for Supabase)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.connection-timeout=30000
```

### 📦 Зависимости

**build.gradle:**
```gradle
implementation 'org.postgresql:postgresql:42.7.1'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
```

### 🔑 Важные детали

1. **SSL обязателен**: `sslmode=require` в URL подключения
2. **Connection Pooler**: Используется порт `6543` (не стандартный `5432`)
3. **HikariCP**: Оптимизирован для работы с Supabase pooler
4. **Username формат**: `postgres.{project-ref}` (не просто `postgres`)

### 🔄 Два режима работы

Приложение поддерживает **гибридный режим**:

1. **Supabase REST API** (`https://fqyklholxklhwydksazc.supabase.co/rest/v1`)
   - Используется для регистрации и аутентификации
   - Легкие операции CRUD
   - Автоматическая валидация через Row Level Security

2. **Direct PostgreSQL (JDBC)** (`jdbc:postgresql://...`)
   - Используется для сложных запросов
   - JPA/Hibernate операции
   - Лучшая производительность для bulk операций

### 🧪 Проверка подключения

```bash
# Тест REST API
curl http://localhost:8089/api/supabase/test

# Тест JDBC подключения
curl http://localhost:8089/api/database/test-connection

# Тест JPA
curl http://localhost:8089/api/jpa-test/test

# Health check
curl http://localhost:8089/api/health
```

### 📊 HikariCP Configuration Explained

- **maximum-pool-size: 10** - Максимум 10 соединений в пуле
- **minimum-idle: 1** - Минимум 1 idle соединение всегда готово
- **idle-timeout: 600000** (10 минут) - Время жизни idle соединения
- **connection-timeout: 30000** (30 секунд) - Таймаут получения соединения из пула

### ⚠️ Важно для Production

1. Используйте переменные окружения для credentials:
   ```properties
   spring.datasource.username=${DB_USERNAME}
   spring.datasource.password=${DB_PASSWORD}
   ```

2. Настройте правильный размер пула для вашей нагрузки

3. Включите SSL сертификаты если требуется дополнительная безопасность

4. Мониторьте Connection Pool метрики через Actuator

### 🔗 Полезные ссылки

- Supabase Connection Pooler: https://supabase.com/docs/guides/database/connecting-to-postgres#connection-pooler
- HikariCP Configuration: https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby
- PostgreSQL JDBC: https://jdbc.postgresql.org/documentation/

