package org.example.new_new_mvp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.new_new_mvp.dto.EmailAuthRequest;
import org.example.new_new_mvp.dto.EmailAuthResponse;
import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.service.EmailAuthService;
import org.example.new_new_mvp.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
@Validated
public class EmailAuthController {
    
    private final EmailAuthService emailAuthService;
    private final JwtUtil jwtUtil;
    
    @PostMapping("/send-code")
    public ResponseEntity<EmailAuthResponse> sendVerificationCode(@Valid @RequestBody EmailAuthRequest request) {
        try {
            emailAuthService.sendVerificationCode(request.getEmail());
            return ResponseEntity.ok(EmailAuthResponse.error("Код подтверждения отправлен на вашу почту"));
        } catch (Exception e) {
            log.error("Error sending verification code", e);
            return ResponseEntity.badRequest()
                .body(EmailAuthResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/verify")
    public ResponseEntity<EmailAuthResponse> verifyCode(@Valid @RequestBody EmailAuthRequest request) {
        try {
            if (request.getVerificationCode() == null || request.getVerificationCode().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(EmailAuthResponse.error("Код подтверждения обязателен"));
            }
            
            User user = emailAuthService.verifyCodeAndLogin(request.getEmail(), request.getVerificationCode());
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
            
            return ResponseEntity.ok(EmailAuthResponse.success(
                token, 
                user.getEmail(), 
                user.getFullName()
            ));
        } catch (Exception e) {
            log.error("Error verifying code", e);
            return ResponseEntity.badRequest()
                .body(EmailAuthResponse.error(e.getMessage()));
        }
    }
}
