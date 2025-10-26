package org.example.new_new_mvp.controller;

import org.example.new_new_mvp.dto.ActivationCodeLoginRequest;
import org.example.new_new_mvp.dto.AuthResponse;
import org.example.new_new_mvp.dto.CreateEmployeeRequest;
import org.example.new_new_mvp.dto.LoginRequest;
import org.example.new_new_mvp.dto.RegisterRequest;
import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            if (response.getToken() != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, null, null, null, e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            if (response.getToken() != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, null, null, null, e.getMessage()));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        try {
            User user = authService.getCurrentUser(authentication.getName());
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Создание сотрудника компанией
     * Требует аутентификации компании
     */
    @PostMapping("/create-employee")
    public ResponseEntity<AuthResponse> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request,
            Authentication authentication) {
        try {
            String companyEmail = authentication.getName();
            AuthResponse response = authService.createEmployee(request, companyEmail);
            
            if (response.getMessage().contains("successfully")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new AuthResponse(null, null, null, null, null, "Error creating employee: " + e.getMessage())
            );
        }
    }
    
    /**
     * Вход сотрудника по activation_code
     * Не требует аутентификации
     */
    @PostMapping("/login-by-code")
    public ResponseEntity<AuthResponse> loginByActivationCode(@Valid @RequestBody ActivationCodeLoginRequest request) {
        try {
            AuthResponse response = authService.loginByActivationCode(request);
            
            if (response.getToken() != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new AuthResponse(null, null, null, null, null, "Login failed: " + e.getMessage())
            );
        }
    }
}
