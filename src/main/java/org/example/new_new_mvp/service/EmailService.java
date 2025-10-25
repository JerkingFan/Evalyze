package org.example.new_new_mvp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.email.from:noreply@evalyze.com}")
    private String fromEmail;
    
    @Value("${app.email.subject:Код подтверждения}")
    private String subject;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public void sendVerificationCode(String toEmail, String verificationCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(buildEmailContent(verificationCode));
            
            mailSender.send(message);
            log.info("Verification code sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification code to: {}", toEmail, e);
            throw new RuntimeException("Не удалось отправить код подтверждения", e);
        }
    }
    
    public void sendInvitationEmail(String email, String invitationCode, String companyName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Приглашение в Evalyze от " + companyName);
            message.setText(buildInvitationEmailContent(invitationCode, companyName));
            
            mailSender.send(message);
            log.info("Invitation email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send invitation email to: {}", email, e);
            throw new RuntimeException("Не удалось отправить приглашение", e);
        }
    }
    
    private String buildEmailContent(String verificationCode) {
        return String.format("""
            Здравствуйте!
            
            Ваш код подтверждения: %s
            
            Код действителен в течение 10 минут.
            Если вы не запрашивали этот код, проигнорируйте это сообщение.
            
            С уважением,
            Команда Evalyze
            """, verificationCode);
    }
    
    private String buildInvitationEmailContent(String invitationCode, String companyName) {
        return String.format("""
            Здравствуйте!
            
            Компания %s приглашает вас присоединиться к платформе Evalyze.
            
            Код приглашения: %s
            
            Перейдите по ссылке для активации: %s/activate?code=%s
            
            С уважением,
            Команда Evalyze
            """, companyName, invitationCode, "https://your-domain.com", invitationCode);
    }
}