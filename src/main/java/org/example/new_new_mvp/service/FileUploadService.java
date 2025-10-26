package org.example.new_new_mvp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.new_new_mvp.model.FileUpload;
import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.repository.FileUploadRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {
    
    private final FileUploadRepository fileUploadRepository;
    
    @Value("${app.upload.path:./uploads}")
    private String uploadPath;
    
    @Value("${app.upload.max-size:10485760}") // 10MB по умолчанию
    private long maxFileSize;
    
    @Value("${app.upload.allowed-types:image/*,application/pdf,text/*,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document}")
    private String allowedTypes;
    
    public List<FileUpload> uploadFiles(User user, List<MultipartFile> files) {
        List<FileUpload> uploadedFiles = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                if (file.isEmpty()) {
                    continue;
                }
                
                // Проверяем размер файла
                if (file.getSize() > maxFileSize) {
                    log.warn("File too large: {} bytes, max allowed: {} bytes", file.getSize(), maxFileSize);
                    continue;
                }
                
                // Проверяем тип файла
                if (!isAllowedFileType(file.getContentType())) {
                    log.warn("File type not allowed: {}", file.getContentType());
                    continue;
                }
                
                log.info("Attempting to save file: {}", file.getOriginalFilename());
                FileUpload fileUpload = saveFile(user, file);
                log.info("File saved successfully: {}", fileUpload != null ? fileUpload.getId() : "null");
                if (fileUpload != null) {
                    uploadedFiles.add(fileUpload);
                    log.info("Added file to uploadedFiles list. Total uploaded: {}", uploadedFiles.size());
                }
                
            } catch (Exception e) {
                log.error("Error uploading file: {} - {}", file.getOriginalFilename(), e.getMessage(), e);
            }
        }
        
        return uploadedFiles;
    }
    
    private FileUpload saveFile(User user, MultipartFile file) throws IOException {
        log.info("saveFile called for user: {}, file: {}", user.getEmail(), file.getOriginalFilename());
        
        try {
            // Создаем директорию для пользователя
            String userDir = user.getId().toString();
            Path userUploadPath = Paths.get(uploadPath, userDir);
            log.info("Creating directory: {}", userUploadPath);
            Files.createDirectories(userUploadPath);
            
            // Генерируем уникальное имя файла
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String storedFileName = generateUniqueFileName(fileExtension);
            log.info("Generated storedFileName: {}", storedFileName);
            
            // Сохраняем файл
            Path filePath = userUploadPath.resolve(storedFileName);
            log.info("Copying file to: {}", filePath);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File copied successfully");
            
            // Создаем запись в базе данных
            FileUpload fileUpload = new FileUpload(
                user,
                originalFileName,
                storedFileName,
                filePath.toString(),
                file.getSize(),
                file.getContentType()
            );
            log.info("Created FileUpload object");
            
            FileUpload saved = fileUploadRepository.save(fileUpload);
            log.info("Saved to database with ID: {}", saved.getId());
            return saved;
            
        } catch (Exception e) {
            log.error("Error in saveFile for file: {}", file.getOriginalFilename(), e);
            throw e;
        }
    }
    
    private String generateUniqueFileName(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s%s", timestamp, uuid, extension);
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
    
    private boolean isAllowedFileType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        
        String[] allowed = allowedTypes.split(",");
        for (String allowedType : allowed) {
            allowedType = allowedType.trim();
            if (allowedType.endsWith("/*")) {
                String baseType = allowedType.substring(0, allowedType.length() - 1);
                if (mimeType.startsWith(baseType)) {
                    return true;
                }
            } else if (mimeType.equals(allowedType)) {
                return true;
            }
        }
        return false;
    }
    
    public List<FileUpload> getUserFiles(User user) {
        return fileUploadRepository.findByUserOrderByUploadedAtDesc(user);
    }
    
    public FileUpload getFileById(UUID fileId) {
        return fileUploadRepository.findById(fileId).orElse(null);
    }
    
    public void deleteFile(UUID fileId) {
        FileUpload fileUpload = fileUploadRepository.findById(fileId).orElse(null);
        if (fileUpload != null) {
            try {
                // Удаляем физический файл
                Files.deleteIfExists(Paths.get(fileUpload.getFilePath()));
                // Удаляем запись из базы данных
                fileUploadRepository.delete(fileUpload);
                log.info("File deleted: {}", fileUpload.getOriginalFileName());
            } catch (IOException e) {
                log.error("Error deleting file: {}", fileUpload.getOriginalFileName(), e);
            }
        }
    }
    
    public List<FileUpload> searchFiles(User user, String searchTerm) {
        return fileUploadRepository.findByUserAndOriginalFileNameContaining(user, searchTerm);
    }
    
    public List<FileUpload> getFilesByType(User user, String mimeType) {
        return fileUploadRepository.findByUserAndMimeTypeContaining(user, mimeType);
    }
}
