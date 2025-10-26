# 🚀 Быстрая настройка ngrok для OAuth

## 1. Скачай ngrok
- Зайди на [ngrok.com](https://ngrok.com/)
- Скачай для Windows
- Распакуй в папку

## 2. Запусти туннель
```bash
# В командной строке:
ngrok http 8089
```

## 3. Получи URL
ngrok покажет:
```
Forwarding    https://abc123.ngrok.io -> http://localhost:8089
```

## 4. Обнови Google Cloud Console
**Authorized redirect URIs:**
```
https://abc123.ngrok.io/oauth-bridge.html
```

## 5. Обнови код
```javascript
const redirectUri = encodeURIComponent('https://abc123.ngrok.io/oauth-bridge.html');
```

## 6. Протестируй
- Открой ngrok URL
- Нажми "Войти через Google"
- Должно работать!
