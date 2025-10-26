# 🌐 Настройка ngrok для Google OAuth

## Что такое ngrok?

ngrok создаёт **безопасный туннель** к твоему локальному серверу, давая ему **публичный URL** с доменным именем.

## 🚀 Установка ngrok

### 1. Скачай ngrok
- Зайди на [ngrok.com](https://ngrok.com/)
- Зарегистрируйся (бесплатно)
- Скачай для Windows

### 2. Установи ngrok
```bash
# Распакуй ngrok.exe в папку (например C:\ngrok\)
# Добавь в PATH или используй полный путь
```

### 3. Запусти туннель
```bash
# В командной строке:
ngrok http 8089

# Или если ngrok не в PATH:
C:\ngrok\ngrok.exe http 8089
```

### 4. Получи публичный URL
ngrok покажет что-то вроде:
```
Forwarding    https://abc123.ngrok.io -> http://localhost:8089
```

**Используй:** `https://abc123.ngrok.io/oauth-bridge.html`

## 🔧 Настройка Google Cloud Console

### 1. Зайди в Google Cloud Console
- **APIs & Services** → **Credentials**
- **Edit** твой OAuth 2.0 Client ID

### 2. Добавь ngrok URL
**Authorized redirect URIs:**
```
https://abc123.ngrok.io/oauth-bridge.html
```

### 3. Обнови код
```javascript
const redirectUri = encodeURIComponent('https://abc123.ngrok.io/oauth-bridge.html');
```

## ⚠️ Важно!

- **ngrok URL меняется** при каждом перезапуске (на бесплатном плане)
- **Для продакшена** нужен статический домен
- **Для разработки** localhost проще

## 🎯 Рекомендация

**Для разработки:** используй `localhost:8089`
**Для продакшена:** купи домен и настрой DNS
