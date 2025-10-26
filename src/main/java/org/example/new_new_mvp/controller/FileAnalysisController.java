package org.example.new_new_mvp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.new_new_mvp.model.FileUpload;
import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.service.FileAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileAnalysisController {
    
    private final FileAnalysisService fileAnalysisService;
    private final org.example.new_new_mvp.repository.UserRepository userRepository;
    
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeFiles(
            @RequestBody Map<String, List<UUID>> request,
            Authentication authentication) {
        
        try {
            // Анализ отключен, так как файлы не сохраняются в БД
            // Webhook уже отправлен в FileUploadController
            log.info("Analysis endpoint called - skipping (webhook already sent)");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "analysis", "Analysis request forwarded to n8n via webhook",
                "message", "Files will be analyzed by n8n workflow"
            ));
            
        } catch (Exception e) {
            log.error("Error in analyze endpoint", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
                ));
        }
    }
    
    @GetMapping("/analysis/{fileId}")
    public ResponseEntity<?> getFileAnalysis(
            @PathVariable UUID fileId,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
            String analysis = fileAnalysisService.getFileAnalysis(user, fileId);
            
            if (analysis == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of(
                "fileId", fileId,
                "analysis", analysis
            ));
            
        } catch (Exception e) {
            log.error("Error getting file analysis", e);
            return ResponseEntity.internalServerError()
                .body("Error retrieving analysis: " + e.getMessage());
        }
    }
}
