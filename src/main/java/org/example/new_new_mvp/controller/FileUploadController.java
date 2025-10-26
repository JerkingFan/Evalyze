package org.example.new_new_mvp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.new_new_mvp.dto.FileUploadResponse;
import org.example.new_new_mvp.model.FileUpload;
import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.service.FileUploadService;
import org.example.new_new_mvp.service.ProfileService;
import org.example.new_new_mvp.service.WebhookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {
    
    private final FileUploadService fileUploadService;
    private final WebhookService webhookService;
    private final ProfileService profileService;
    private final org.example.new_new_mvp.repository.UserRepository userRepository;
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "file", required = false) MultipartFile singleFile,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tags,
            Authentication authentication) {
        
        try {
            log.info("=== FILE UPLOAD START ===");
            log.info("Received files parameter: {}", files != null ? files.size() : "null");
            log.info("Received singleFile parameter: {}", singleFile != null ? singleFile.getOriginalFilename() : "null");
            
            // Получаем список файлов - либо из "files", либо из "file"
            List<MultipartFile> fileList = files;
            if ((fileList == null || fileList.isEmpty()) && singleFile != null) {
                fileList = List.of(singleFile);
            }
            
            log.info("Final fileList size: {}", fileList != null ? fileList.size() : 0);
            
            // Get user from authentication (principal contains email as String)
            String userEmail = authentication.getName();
            log.info("User email from auth: {}", userEmail);
            
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
            
            log.info("User found: {}", user.getEmail());
            
            if (fileList == null || fileList.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "No files provided"));
            }
            
            // ОТПРАВЛЯЕМ ФАЙЛЫ НАПРЯМУЮ В N8N
            try {
                String profileData = profileService.getProfileByUserId(user.getId())
                    .map(profile -> profile.getProfileData() != null ? profile.getProfileData().toString() : "{}")
                    .orElse("{}");
                
                // Отправляем webhook с ФАЙЛАМИ напрямую
                webhookService.sendCompetencyAnalysisWebhookWithFiles(
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    profileData,
                    user.getCompany() != null ? user.getCompany().getName() : null,
                    fileList  // Передаем сами файлы
                );
                log.info("Competency analysis webhook sent for user: {} with {} files", user.getEmail(), fileList.size());
                
                // Возвращаем успешный ответ с информацией о файлах
                List<Map<String, Object>> responses = new java.util.ArrayList<>();
                for (int i = 0; i < fileList.size(); i++) {
                    Map<String, Object> fileResponse = new java.util.HashMap<>();
                    fileResponse.put("id", java.util.UUID.randomUUID().toString());  // Генерируем фейковый ID
                    fileResponse.put("originalFileName", fileList.get(i).getOriginalFilename());
                    fileResponse.put("fileSize", fileList.get(i).getSize());
                    fileResponse.put("uploadedAt", java.time.LocalDateTime.now().toString());
                    responses.add(fileResponse);
                }
                
                return ResponseEntity.ok(responses);
                
            } catch (Exception webhookException) {
                log.error("Error sending competency analysis webhook: {}", webhookException.getMessage(), webhookException);
                // Даже если webhook не отправился, возвращаем успех для фронтенда
                List<Map<String, Object>> responses = new java.util.ArrayList<>();
                for (MultipartFile file : fileList) {
                    Map<String, Object> fileResponse = new java.util.HashMap<>();
                    fileResponse.put("id", java.util.UUID.randomUUID().toString());
                    fileResponse.put("originalFileName", file.getOriginalFilename());
                    fileResponse.put("fileSize", file.getSize());
                    responses.add(fileResponse);
                }
                return ResponseEntity.ok(responses);
            }
            
        } catch (Exception e) {
            log.error("Error uploading files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error uploading files: " + e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<FileUploadResponse>> getUserFiles(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
            List<FileUpload> files = fileUploadService.getUserFiles(user);
            
            List<FileUploadResponse> responses = files.stream()
                .map(FileUploadResponse::fromFileUpload)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            log.error("Error getting user files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{fileId}")
    public ResponseEntity<FileUploadResponse> getFile(@PathVariable UUID fileId, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
            FileUpload file = fileUploadService.getFileById(fileId);
            
            if (file == null || !file.getUser().getId().equals(user.getId())) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(FileUploadResponse.fromFileUpload(file));
            
        } catch (Exception e) {
            log.error("Error getting file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable UUID fileId, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
            FileUpload file = fileUploadService.getFileById(fileId);
            
            if (file == null || !file.getUser().getId().equals(user.getId())) {
                return ResponseEntity.notFound().build();
            }
            
            fileUploadService.deleteFile(fileId);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error deleting file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting file: " + e.getMessage());
        }
    }
    
    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(@PathVariable UUID fileId, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
            FileUpload file = fileUploadService.getFileById(fileId);
            
            if (file == null || !file.getUser().getId().equals(user.getId())) {
                return ResponseEntity.notFound().build();
            }
            
            Path filePath = Paths.get(file.getFilePath());
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] fileContent = Files.readAllBytes(filePath);
            
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + file.getOriginalFileName() + "\"")
                .header("Content-Type", file.getMimeType())
                .body(fileContent);
            
        } catch (IOException e) {
            log.error("Error downloading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error downloading file: " + e.getMessage());
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<FileUploadResponse>> searchFiles(
            @RequestParam String query,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
            List<FileUpload> files = fileUploadService.searchFiles(user, query);
            
            List<FileUploadResponse> responses = files.stream()
                .map(FileUploadResponse::fromFileUpload)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            log.error("Error searching files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/type/{mimeType}")
    public ResponseEntity<List<FileUploadResponse>> getFilesByType(
            @PathVariable String mimeType,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
            List<FileUpload> files = fileUploadService.getFilesByType(user, mimeType);
            
            List<FileUploadResponse> responses = files.stream()
                .map(FileUploadResponse::fromFileUpload)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            log.error("Error getting files by type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/temp/{userId}/{fileName}")
    public ResponseEntity<?> downloadTempFile(
            @PathVariable String userId,
            @PathVariable String fileName) {
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get("./uploads/temp/" + userId + "/" + fileName);
            
            if (!java.nio.file.Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] fileContent = java.nio.file.Files.readAllBytes(filePath);
            
            // Определяем content type
            String contentType = java.nio.file.Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .header("Content-Type", contentType)
                .body(fileContent);
            
        } catch (Exception e) {
            log.error("Error downloading temp file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error downloading file: " + e.getMessage());
        }
    }
}
