package org.example.new_new_mvp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.new_new_mvp.model.EmailVerification;
import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.model.UserRole;
import org.example.new_new_mvp.repository.EmailVerificationRepository;
import org.example.new_new_mvp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAuthService {
    
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    private static final Random random = new Random();
    
    public void sendVerificationCode(String email) {
        // Проверяем, есть ли уже активный код для этого email
        Optional<EmailVerification> existingVerification = emailVerificationRepository
            .findLatestActiveByEmail(email, LocalDateTime.now());
        
        if (existingVerification.isPresent()) {
            EmailVerification verification = existingVerification.get();
            if (!verification.isExpired()) {
                throw new RuntimeException("Код уже отправлен. Попробуйте позже.");
            }
        }
        
        // Генерируем новый код
        String verificationCode = generateVerificationCode();
        
        // Создаем новую запись верификации
        EmailVerification verification = new EmailVerification(email, verificationCode);
        emailVerificationRepository.save(verification);
        
        // Отправляем код на email
        emailService.sendVerificationCode(email, verificationCode);
        
        log.info("Verification code sent to: {}", email);
    }
    
    @Transactional
    public User verifyCodeAndLogin(String email, String verificationCode) {
        // Находим активную верификацию
        Optional<EmailVerification> verificationOpt = emailVerificationRepository
            .findByEmailAndVerificationCodeAndIsUsedFalse(email, verificationCode);
        
        if (verificationOpt.isEmpty()) {
            throw new RuntimeException("Неверный код подтверждения");
        }
        
        EmailVerification verification = verificationOpt.get();
        
        // Проверяем, не истек ли код
        if (verification.isExpired()) {
            throw new RuntimeException("Код подтверждения истек");
        }
        
        // Проверяем количество попыток
        if (verification.isMaxAttemptsReached()) {
            throw new RuntimeException("Превышено количество попыток ввода кода");
        }
        
        // Увеличиваем счетчик попыток
        verification.incrementAttempts();
        emailVerificationRepository.save(verification);
        
        // Если код неверный, выбрасываем исключение
        if (!verification.getVerificationCode().equals(verificationCode)) {
            throw new RuntimeException("Неверный код подтверждения");
        }
        
        // Помечаем код как использованный
        verification.setUsed(true);
        emailVerificationRepository.save(verification);
        
        // Помечаем все остальные коды для этого email как использованные
        emailVerificationRepository.markAllAsUsedByEmail(email);
        
        // Создаем или находим пользователя
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> createNewUser(email));
        
        log.info("User logged in via email verification: {}", email);
        return user;
    }
    
    private User createNewUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setRole(UserRole.EMPLOYEE);
        user.setFullName(extractNameFromEmail(email));
        return userRepository.save(user);
    }
    
    private String extractNameFromEmail(String email) {
        String name = email.split("@")[0];
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
    
    private String generateVerificationCode() {
        return String.format("%06d", random.nextInt(1000000));
    }
    
    @Transactional
    public void cleanupExpiredVerifications() {
        emailVerificationRepository.deleteExpiredVerifications(LocalDateTime.now());
        log.info("Cleaned up expired email verifications");
    }
}
