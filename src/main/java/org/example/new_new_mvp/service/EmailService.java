package org.example.new_new_mvp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    public void sendInvitationEmail(String to, String invitationCode, String companyName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Приглашение в Evalyze от " + companyName);
        message.setText("Добро пожаловать в Evalyze!\n\n" +
                       "Вы получили приглашение от компании " + companyName + ".\n\n" +
                       "Код приглашения: " + invitationCode + "\n\n" +
                       "Перейдите по ссылке для завершения регистрации:\n" +
                       "http://localhost:8089/accept-invitation?code=" + invitationCode);
        
        mailSender.send(message);
    }
    
    public void sendVerificationCode(String to, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Код подтверждения для Evalyze");
        message.setText("Ваш код подтверждения: " + verificationCode + 
                       "\n\nИспользуйте этот код для завершения регистрации профиля.");
        
        mailSender.send(message);
    }
    
    public void sendProfileGenerated(String to, String profileData) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Ваш профиль готов!");
        message.setText("Ваш IT-профиль был успешно сгенерирован.\n\n" + profileData);
        
        mailSender.send(message);
    }
}
