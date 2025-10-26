package org.example.new_new_mvp.controller;

import org.example.new_new_mvp.dto.AuthResponse;
import org.example.new_new_mvp.service.GoogleOAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/google")
public class GoogleOAuthController {

    @Autowired
    private GoogleOAuthService googleOAuthService;

    /**
     * Получить URL для авторизации через Google
     */
    @GetMapping("/url")
    public ResponseEntity<String> getAuthorizationUrl() {
        String authUrl = googleOAuthService.getAuthorizationUrl();
        return ResponseEntity.ok(authUrl);
    }

    /**
     * Callback после авторизации Google через n8n webhook
     * Принимает токен от фронтенда после авторизации через n8n
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticateWithGoogle(@RequestBody GoogleAuthRequest request) {
        try {
            System.out.println("Google authentication request received");
            AuthResponse response = googleOAuthService.authenticateWithGoogle(request.getToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Google authentication error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null, null, null, "Error: " + e.getMessage()));
        }
    }

    /**
     * Обмен authorization code на JWT токен (для oauth-bridge.html)
     * Принимает код от Google и возвращает JWT + данные пользователя
     */
    @PostMapping("/exchange")
    public ResponseEntity<AuthResponse> exchangeCodeForToken(@RequestBody ExchangeCodeRequest request) {
        try {
            System.out.println("=== Exchange code for token START ===");
            System.out.println("Code received from oauth-bridge.html");
            
            AuthResponse response = googleOAuthService.exchangeCodeForToken(request.getCode());
            
            System.out.println("=== Exchange SUCCESS ===");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("=== Exchange ERROR ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null, null, null, "Error: " + e.getMessage()));
        }
    }

    /**
     * Callback для редиректа от n8n (опциональный - не используется при bridge.html)
     * Обрабатывает GET запрос с параметрами от n8n
     */
    @GetMapping("/callback")
    public ResponseEntity<String> handleN8nCallback(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name) {
        try {
            if (token != null && !token.isEmpty()) {
                // Если n8n возвращает данные напрямую, обрабатываем их
                AuthResponse response = googleOAuthService.authenticateWithGoogle(token);
                
                // Перенаправляем на фронтенд с токеном
                String redirectUrl = String.format(
                    "/?token=%s&email=%s&role=%s&fullName=%s",
                    response.getToken(),
                    response.getEmail(),
                    response.getRole(),
                    response.getFullName()
                );
                
                return ResponseEntity.status(302)
                        .header("Location", redirectUrl)
                        .build();
            } else {
                throw new RuntimeException("No token received from n8n");
            }
        } catch (Exception e) {
            System.out.println("n8n callback error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(302)
                    .header("Location", "/?error=" + e.getMessage())
                    .build();
        }
    }

    // DTO для обмена кода
    public static class ExchangeCodeRequest {
        private String code;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    // DTO для аутентификации с Google токеном
    public static class GoogleAuthRequest {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}

