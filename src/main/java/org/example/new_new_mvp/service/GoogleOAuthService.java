package org.example.new_new_mvp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.new_new_mvp.dto.AuthResponse;
import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.model.UserRole;
import org.example.new_new_mvp.repository.UserRepository;
import org.example.new_new_mvp.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoogleOAuthService {

    // URL n8n webhook
    // Production URL - требует активного workflow в n8n
    private static final String N8N_WEBHOOK_URL = "https://guglovskij.app.n8n.cloud/webhook/07a96af0-9f1f-44e7-bad3-86a2c4e0cb28";
    
    // Если Production не работает, временно используй Test URL:
    // 1. В n8n открой Webhook node
    // 2. Нажми "Listen for Test Event"
    // 3. Скопируй Test URL и замени выше
    // Например: https://guglovskij.app.n8n.cloud/webhook-test/...

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Инициирует авторизацию через Google через локальный bridge
     * Возвращает URL локальной bridge страницы
     */
    public String getAuthorizationUrl() {
        // Используем локальную bridge страницу вместо n8n
        String bridgeUrl = "http://5.83.140.54:8089/oauth-bridge.html";
        System.out.println("Redirecting to local OAuth bridge: " + bridgeUrl);
        return bridgeUrl;
    }

    /**
     * Обрабатывает данные пользователя от n8n webhook
     * @param googleToken - Google access token или данные от n8n
     */
    public AuthResponse authenticateWithGoogle(String googleToken) {
        try {
            System.out.println("=== Google OAuth via n8n START ===");
            System.out.println("Token received: " + (googleToken != null ? googleToken.substring(0, Math.min(20, googleToken.length())) + "..." : "null"));

            // Отправляем запрос в n8n webhook для получения данных пользователя
            GoogleUserInfo userInfo = getUserInfoFromN8n(googleToken);
            System.out.println("User info from n8n: email=" + userInfo.email + ", name=" + userInfo.name);

            // Находим или создаем пользователя
            User user = findOrCreateUser(userInfo, googleToken);
            System.out.println("User found/created: " + user.getEmail());

            // Генерируем JWT токен
            String jwtToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

            System.out.println("=== Google OAuth via n8n SUCCESS ===");
            
            return new AuthResponse(
                    jwtToken,
                    user.getEmail(),
                    user.getRole(),
                    user.getFullName(),
                    null,
                    "Login successful"
            );
        } catch (Exception e) {
            System.out.println("=== Google OAuth via n8n ERROR ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("OAuth via n8n failed: " + e.getMessage(), e);
        }
    }

    /**
     * Получает информацию о пользователе через n8n webhook
     */
    private GoogleUserInfo getUserInfoFromN8n(String googleToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Формируем тело запроса
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("token", googleToken);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Отправляем POST запрос в n8n webhook
            ResponseEntity<String> response = restTemplate.postForEntity(N8N_WEBHOOK_URL, request, String.class);
            String responseBody = response.getBody();
            
            System.out.println("n8n webhook response: " + responseBody);

            // Парсим ответ
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            GoogleUserInfo userInfo = new GoogleUserInfo();
            // Ожидаем, что n8n вернёт email, name и возможно picture
            userInfo.email = jsonNode.has("email") ? jsonNode.get("email").asText() : null;
            userInfo.name = jsonNode.has("name") ? jsonNode.get("name").asText() : 
                           (jsonNode.has("fullName") ? jsonNode.get("fullName").asText() : userInfo.email);
            userInfo.picture = jsonNode.has("picture") ? jsonNode.get("picture").asText() : null;

            if (userInfo.email == null || userInfo.email.isEmpty()) {
                throw new RuntimeException("Email not received from n8n webhook");
            }

            return userInfo;
        } catch (Exception e) {
            System.out.println("Error getting user info from n8n: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get user info from n8n webhook", e);
        }
    }

    /**
     * Находит существующего пользователя или создает нового
     */
    private User findOrCreateUser(GoogleUserInfo userInfo, String googleToken) {
        Optional<User> existingUser = userRepository.findByEmail(userInfo.email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Google OAuth token removed - using email auth instead
            return userRepository.save(user);
        }

        // Создаем нового пользователя
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setEmail(userInfo.email);
        newUser.setFullName(userInfo.name);
        newUser.setRole(UserRole.EMPLOYEE); // По умолчанию роль EMPLOYEE
        // Google OAuth token removed - using email auth instead

        return userRepository.save(newUser);
    }

    /**
     * Обменивает authorization code на access token напрямую (для oauth-bridge.html)
     * Используется когда фронтенд получает код от Google напрямую
     */
    public AuthResponse exchangeCodeForToken(String code) {
        try {
            System.out.println("=== Direct Code Exchange START ===");
            System.out.println("Code: " + (code != null ? code.substring(0, Math.min(20, code.length())) + "..." : "null"));

            // Константы для прямого обмена
            final String CLIENT_ID = "340752343067-79ipapn7o97qd8ibqvgpjg4687fm7jo7.apps.googleusercontent.com";
            final String CLIENT_SECRET = ""; // Опционально для Public clients
            final String REDIRECT_URI = "https://24beface.ru/oauth-bridge.html";
            final String TOKEN_URI = "https://oauth2.googleapis.com/token";
            final String USER_INFO_URI = "https://www.googleapis.com/oauth2/v3/userinfo";

            // 1. Обмениваем код на токен
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            Map<String, String> tokenParams = new HashMap<>();
            tokenParams.put("code", code);
            tokenParams.put("client_id", CLIENT_ID);
            if (CLIENT_SECRET != null && !CLIENT_SECRET.isEmpty()) {
                tokenParams.put("client_secret", CLIENT_SECRET);
            }
            tokenParams.put("redirect_uri", REDIRECT_URI);
            tokenParams.put("grant_type", "authorization_code");

            // Формируем form-urlencoded body
            StringBuilder formBody = new StringBuilder();
            for (Map.Entry<String, String> entry : tokenParams.entrySet()) {
                if (formBody.length() > 0) {
                    formBody.append("&");
                }
                formBody.append(entry.getKey()).append("=").append(java.net.URLEncoder.encode(entry.getValue(), "UTF-8"));
            }

            HttpEntity<String> tokenRequest = new HttpEntity<>(formBody.toString(), tokenHeaders);
            
            System.out.println("Exchanging code for token...");
            ResponseEntity<String> tokenResponse = restTemplate.postForEntity(TOKEN_URI, tokenRequest, String.class);
            JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
            String accessToken = tokenJson.get("access_token").asText();
            
            System.out.println("Access token obtained: " + accessToken.substring(0, 20) + "...");

            // 2. Получаем информацию о пользователе
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.setBearerAuth(accessToken);
            HttpEntity<?> userInfoRequest = new HttpEntity<>(userInfoHeaders);

            System.out.println("Getting user info from Google...");
            ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                USER_INFO_URI,
                org.springframework.http.HttpMethod.GET,
                userInfoRequest,
                String.class
            );

            JsonNode userJson = objectMapper.readTree(userInfoResponse.getBody());
            
            GoogleUserInfo userInfo = new GoogleUserInfo();
            userInfo.email = userJson.get("email").asText();
            userInfo.name = userJson.has("name") ? userJson.get("name").asText() : userInfo.email;
            userInfo.picture = userJson.has("picture") ? userJson.get("picture").asText() : null;

            System.out.println("User info: " + userInfo.email);

            // 3. Создаём/находим пользователя
            User user = findOrCreateUser(userInfo, accessToken);
            System.out.println("User found/created: " + user.getEmail());

            // 4. Генерируем JWT токен
            String jwtToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

            System.out.println("=== Direct Code Exchange SUCCESS ===");

            return new AuthResponse(
                    jwtToken,
                    user.getEmail(),
                    user.getRole(),
                    user.getFullName(),
                    null, // companyId - для EMPLOYEE всегда null
                    "Login successful"
            );
        } catch (Exception e) {
            System.out.println("=== Direct Code Exchange ERROR ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Code exchange failed: " + e.getMessage(), e);
        }
    }

    /**
     * DTO для информации о пользователе Google
     */
    private static class GoogleUserInfo {
        String email;
        String name;
        String picture;
    }
}

