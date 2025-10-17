# Деплой Evalyze

## На сервере выполни ОДИН РАЗ:

```bash
# 1. Создай базу данных
sudo -u postgres psql -c "CREATE DATABASE evalyze;"

# 2. Включи UUID расширение
sudo -u postgres psql -d evalyze -c "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";"

# 3. Создай таблицы
sudo -u postgres psql -d evalyze -f schema-postgresql.sql

# 4. Загрузи тестовые данные
sudo -u postgres psql -d evalyze -f data-postgresql.sql
```

### Если нет sudo доступа:

```bash
# Войди как postgres пользователь
su - postgres

# Затем выполни:
psql -c "CREATE DATABASE evalyze;"
psql -d evalyze -c "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";"
psql -d evalyze -f schema-postgresql.sql
psql -d evalyze -f data-postgresql.sql

# Выйди
exit
```

## Запуск приложения

```bash
# Установи переменные окружения
export DB_USERNAME=postgres
export DB_PASSWORD=твой_пароль

# Запусти JAR
java -jar NEW_NEW_MVP-0.0.1-SNAPSHOT.jar
```

Приложение будет доступно на `http://your-server:8089`

---

## Что делает приложение:

- ✅ Подключается к базе данных `evalyze`
- ✅ Проверяет схему таблиц (`hibernate.ddl-auto=validate`)
- ✅ Работает с данными через Spring Data JPA
- ✅ Никаких скриптов в JAR - всё уже на сервере

---

## Если база данных не создана - получишь ошибку:

```
FATAL: database "evalyze" does not exist
```

**Решение:** Выполни 4 команды выше (ОДИН РАЗ на сервере)
