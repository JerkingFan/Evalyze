package org.example.new_new_mvp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class WebhookService {
    
    private static final String N8N_WEBHOOK_URL = "http://5.83.140.54:5678/webhook/31435a9e-5b49-4918-b37c-001b27e51335";
    
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
            
            // Send webhook to n8n
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> request = new HttpEntity<>(webhookPayload, headers);
            
            System.out.println("Sending webhook to n8n: " + N8N_WEBHOOK_URL);
            System.out.println("Webhook payload: " + webhookPayload);
            
            String response = restTemplate.postForObject(N8N_WEBHOOK_URL, request, String.class);
            
            System.out.println("Webhook response: " + response);
            
            return response;
            
        } catch (Exception e) {
            System.out.println("Error sending webhook to n8n: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send webhook: " + e.getMessage());
        }
    }
}
