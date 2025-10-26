package org.example.new_new_mvp.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WebhookService {
    
    // Новые webhook URL-ы для трёх кнопок
    private static final String WEBHOOK_ANALYZE_COMPETENCIES = "https://guglovskij.app.n8n.cloud/webhook/0d0a654b-772e-447a-9223-8b443f788175";
    private static final String WEBHOOK_ASSIGN_JOB_ROLE = "https://guglovskij.app.n8n.cloud/webhook/113447c6-c39e-410c-ab15-4f5ab7809fd9";
    private static final String WEBHOOK_GENERATE_AI_PROFILE = "https://guglovskij.app.n8n.cloud/webhook/bbd2959f-bedc-43fc-a558-69c0fe7b4db";
    
    // Старый webhook для обратной совместимости
    private static final String N8N_WEBHOOK_URL = "http://5.83.140.54:5678/webhook/31435a9e-5b49-4918-b37c-001b27e51335";
    
    /**
     * Кнопка 1: Загрузить и анализировать компетенции
     * Отправляет информацию о файлах и данных пользователя для анализа
     */
    public String sendCompetencyAnalysisWebhook(UUID userId, String userEmail, String userName, 
                                               String profileData, String companyName) {
        try {
            String webhookPayload = String.format("""
                {
                    "action": "analyze_competencies",
                    "userId": "%s",
                    "userEmail": "%s",
                    "userName": "%s",
                    "profileData": %s,
                    "companyName": "%s",
                    "timestamp": "%s"
                }
                """,
                userId,
                userEmail != null ? userEmail : "",
                userName != null ? userName : "",
                profileData != null ? profileData : "{}",
                companyName != null ? companyName : "",
                LocalDateTime.now().toString()
            );
            
            return sendWebhook(WEBHOOK_ANALYZE_COMPETENCIES, webhookPayload);
            
        } catch (Exception e) {
            System.out.println("Error sending competency analysis webhook: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send webhook: " + e.getMessage());
        }
    }
    
    /**
     * Кнопка 1: Загрузить и анализировать компетенции (С ФАЙЛАМИ В BASE64)
     * Отправляет файлы в base64 и данные пользователя для анализа через JSON
     */
    public String sendCompetencyAnalysisWebhookWithFiles(UUID userId, String userEmail, String userName, 
                                                         String profileData, String companyName, 
                                                         List<MultipartFile> files) {
        try {
            // Формируем JSON массив файлов с base64
            StringBuilder filesJson = new StringBuilder("[");
            if (files != null && !files.isEmpty()) {
                for (int i = 0; i < files.size(); i++) {
                    MultipartFile file = files.get(i);
                    if (i > 0) filesJson.append(",");
                    
                    // Кодируем файл в base64
                    String base64Content = java.util.Base64.getEncoder().encodeToString(file.getBytes());
                    
                    filesJson.append(String.format("""
                        {
                            "originalFileName": "%s",
                            "size": %d,
                            "contentType": "%s",
                            "content": "%s"
                        }
                        """,
                        file.getOriginalFilename() != null ? file.getOriginalFilename().replace("\"", "\\\"") : "",
                        file.getSize(),
                        file.getContentType() != null ? file.getContentType().replace("\"", "\\\"") : "",
                        base64Content
                    ));
                }
            }
            filesJson.append("]");
            
            String webhookPayload = String.format("""
                {
                    "action": "analyze_competencies",
                    "userId": "%s",
                    "userEmail": "%s",
                    "userName": "%s",
                    "profileData": %s,
                    "companyName": "%s",
                    "files": %s,
                    "timestamp": "%s"
                }
                """,
                userId,
                userEmail != null ? userEmail : "",
                userName != null ? userName : "",
                profileData != null ? profileData : "{}",
                companyName != null ? companyName : "",
                filesJson.toString(),
                LocalDateTime.now().toString()
            );
            
            System.out.println("=== WEBHOOK JSON WITH BASE64 FILES ===");
            System.out.println("URL: " + WEBHOOK_ANALYZE_COMPETENCIES);
            System.out.println("Files count: " + (files != null ? files.size() : 0));
            System.out.println("User: " + userEmail);
            System.out.println("Payload size: " + webhookPayload.length() + " bytes");
            
            String response = sendWebhook(WEBHOOK_ANALYZE_COMPETENCIES, webhookPayload);
            
            return response;
            
        } catch (Exception e) {
            System.out.println("=== WEBHOOK ERROR ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.out.println("=== END ERROR ===");
            throw new RuntimeException("Failed to send webhook: " + e.getMessage());
        }
    }
    
    /**
     * Кнопка 1: Загрузить и анализировать компетенции (с информацией о файлах)
     * Отправляет информацию о файлах и данных пользователя для анализа
     */
    public String sendCompetencyAnalysisWebhook(UUID userId, String userEmail, String userName, 
                                               String profileData, String companyName, 
                                               List<Map<String, Object>> files) {
        try {
            // Формируем JSON массив файлов
            StringBuilder filesJson = new StringBuilder("[");
            if (files != null && !files.isEmpty()) {
                for (int i = 0; i < files.size(); i++) {
                    Map<String, Object> file = files.get(i);
                    if (i > 0) filesJson.append(",");
                    filesJson.append(String.format("""
                        {
                            "originalFileName": "%s",
                            "storedFileName": "%s",
                            "size": %d,
                            "contentType": "%s",
                            "downloadUrl": "%s"
                        }
                        """,
                        file.get("originalFileName") != null ? file.get("originalFileName").toString().replace("\"", "\\\"") : "",
                        file.get("storedFileName") != null ? file.get("storedFileName").toString().replace("\"", "\\\"") : "",
                        file.get("size") != null ? file.get("size") : 0,
                        file.get("contentType") != null ? file.get("contentType").toString().replace("\"", "\\\"") : "",
                        file.get("downloadUrl") != null ? file.get("downloadUrl").toString().replace("\"", "\\\"") : ""
                    ));
                }
            }
            filesJson.append("]");
            
            String webhookPayload = String.format("""
                {
                    "action": "analyze_competencies",
                    "userId": "%s",
                    "userEmail": "%s",
                    "userName": "%s",
                    "profileData": %s,
                    "companyName": "%s",
                    "files": %s,
                    "timestamp": "%s"
                }
                """,
                userId,
                userEmail != null ? userEmail : "",
                userName != null ? userName : "",
                profileData != null ? profileData : "{}",
                companyName != null ? companyName : "",
                filesJson.toString(),
                LocalDateTime.now().toString()
            );
            
            return sendWebhook(WEBHOOK_ANALYZE_COMPETENCIES, webhookPayload);
            
        } catch (Exception e) {
            System.out.println("Error sending competency analysis webhook: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send webhook: " + e.getMessage());
        }
    }
    
    /**
     * Кнопка 2: Выбрать роль из списка
     * Отправляет информацию о назначенной роли
     */
    public String sendJobRoleAssignmentWebhook(UUID userId, String userEmail, String userName,
                                               UUID jobRoleId, String jobRoleTitle, String jobRoleDescription,
                                               String profileData, String companyName) {
        try {
            String webhookPayload = String.format("""
                {
                    "action": "assign_job_role",
                    "userId": "%s",
                    "userEmail": "%s",
                    "userName": "%s",
                    "jobRoleId": "%s",
                    "jobRoleTitle": "%s",
                    "jobRoleDescription": "%s",
                    "profileData": %s,
                    "companyName": "%s",
                    "timestamp": "%s"
                }
                """,
                userId,
                userEmail != null ? userEmail : "",
                userName != null ? userName : "",
                jobRoleId != null ? jobRoleId.toString() : "",
                jobRoleTitle != null ? jobRoleTitle : "",
                jobRoleDescription != null ? jobRoleDescription : "",
                profileData != null ? profileData : "{}",
                companyName != null ? companyName : "",
                LocalDateTime.now().toString()
            );
            
            return sendWebhook(WEBHOOK_ASSIGN_JOB_ROLE, webhookPayload);
            
        } catch (Exception e) {
            System.out.println("Error sending job role assignment webhook: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send webhook: " + e.getMessage());
        }
    }

    // Overload: include activationCode
    public String sendJobRoleAssignmentWebhook(UUID userId, String userEmail, String userName,
                                               String activationCode,
                                               UUID jobRoleId, String jobRoleTitle, String jobRoleDescription,
                                               String profileData, String companyName) {
        try {
            String webhookPayload = String.format("""
                {
                    "action": "assign_job_role",
                    "userId": "%s",
                    "userEmail": "%s",
                    "userName": "%s",
                    "activationCode": "%s",
                    "jobRoleId": "%s",
                    "jobRoleTitle": "%s",
                    "jobRoleDescription": "%s",
                    "profileData": %s,
                    "companyName": "%s",
                    "timestamp": "%s"
                }
                """,
                userId,
                userEmail != null ? userEmail : "",
                userName != null ? userName : "",
                activationCode != null ? activationCode : "",
                jobRoleId != null ? jobRoleId.toString() : "",
                jobRoleTitle != null ? jobRoleTitle : "",
                jobRoleDescription != null ? jobRoleDescription : "",
                profileData != null ? profileData : "{}",
                companyName != null ? companyName : "",
                LocalDateTime.now().toString()
            );

            System.out.println("=== SENDING JOB ROLE WEBHOOK ===");
            System.out.println("URL: " + WEBHOOK_ASSIGN_JOB_ROLE);
            System.out.println("Payload: " + webhookPayload);

            return sendWebhook(WEBHOOK_ASSIGN_JOB_ROLE, webhookPayload);
        } catch (Exception e) {
            System.out.println("Error sending job role assignment webhook: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send webhook: " + e.getMessage());
        }
    }
    
    /**
     * Кнопка 3: Создать мой AI-профиль
     * Отправляет запрос на генерацию AI-профиля со всеми данными пользователя
     */
    public String sendAIProfileGenerationWebhook(UUID userId, String userEmail, String userName,
                                                String profileData, String companyName, String activationCode,
                                                String telegramChatId, String status) {
        try {
            String webhookPayload = String.format("""
                {
                    "action": "generate_ai_profile",
                    "userId": "%s",
                    "userEmail": "%s",
                    "userName": "%s",
                    "activationCode": "%s",
                    "telegramChatId": "%s",
                    "status": "%s",
                    "profileData": %s,
                    "companyName": "%s",
                    "timestamp": "%s"
                }
                """,
                userId,
                userEmail != null ? userEmail : "",
                userName != null ? userName : "",
                activationCode != null ? activationCode : "",
                telegramChatId != null ? telegramChatId : "",
                status != null ? status : "",
                profileData != null ? profileData : "{}",
                companyName != null ? companyName : "",
                LocalDateTime.now().toString()
            );
            
            System.out.println("=== SENDING AI PROFILE GENERATION WEBHOOK ===");
            System.out.println("URL: " + WEBHOOK_GENERATE_AI_PROFILE);
            System.out.println("Payload:");
            System.out.println(webhookPayload);
            
            return sendWebhook(WEBHOOK_GENERATE_AI_PROFILE, webhookPayload);
            
        } catch (Exception e) {
            System.out.println("Error sending AI profile generation webhook: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send webhook: " + e.getMessage());
        }
    }
    
    /**
     * Универсальный метод для отправки webhook'а
     */
    private String sendWebhook(String webhookUrl, String payload) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> request = new HttpEntity<>(payload, headers);
            
            System.out.println("=== WEBHOOK SEND START ===");
            System.out.println("URL: " + webhookUrl);
            System.out.println("Payload:");
            System.out.println(payload);
            
            String response = restTemplate.postForObject(webhookUrl, request, String.class);
            
            System.out.println("=== WEBHOOK RESPONSE ===");
            System.out.println(response);
            System.out.println("=== END ===");
            
            return response;
            
        } catch (Exception e) {
            System.out.println("=== WEBHOOK ERROR ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.out.println("=== END ERROR ===");
            throw e;
        }
    }
    
    /**
     * Старый метод для обратной совместимости
     */
    public String sendProfileWebhook(UUID profileId, String userEmail, String userName, 
                                   String profileData, String companyName) {
        try {
            // Prepare webhook payload
            String webhookPayload = String.format("""
                {
                    "profileId": "%s",
                    "userEmail": "%s",
                    "userName": "%s",
                    "profileData": %s,
                    "companyName": "%s",
                    "timestamp": "%s"
                }
                """,
                profileId,
                userEmail != null ? userEmail : "",
                userName != null ? userName : "",
                profileData != null ? profileData : "{}",
                companyName != null ? companyName : "",
                LocalDateTime.now().toString()
            );
            
            return sendWebhook(N8N_WEBHOOK_URL, webhookPayload);
            
        } catch (Exception e) {
            System.out.println("Error sending webhook to n8n: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send webhook: " + e.getMessage());
        }
    }
}
