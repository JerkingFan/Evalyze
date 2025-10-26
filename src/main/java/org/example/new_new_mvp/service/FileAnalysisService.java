package org.example.new_new_mvp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.new_new_mvp.model.FileUpload;
import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.repository.FileUploadRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileAnalysisService {
    
    private final FileUploadRepository fileUploadRepository;
    
    public String analyzeFiles(User user, List<UUID> fileIds) {
        log.info("Analyzing {} files for user: {}", fileIds.size(), user.getEmail());
        
        StringBuilder analysisResult = new StringBuilder();
        analysisResult.append("=== АНАЛИЗ ФАЙЛОВ ===\n\n");
        analysisResult.append("Пользователь: ").append(user.getFullName()).append("\n");
        analysisResult.append("Email: ").append(user.getEmail()).append("\n");
        analysisResult.append("Дата анализа: ").append(java.time.LocalDateTime.now()).append("\n\n");
        
        for (UUID fileId : fileIds) {
            try {
                FileUpload file = fileUploadRepository.findById(fileId).orElse(null);
                if (file == null || !file.getUser().getId().equals(user.getId())) {
                    continue;
                }
                
                analysisResult.append("--- Файл: ").append(file.getOriginalFileName()).append(" ---\n");
                analysisResult.append("Тип: ").append(file.getMimeType()).append("\n");
                analysisResult.append("Размер: ").append(formatFileSize(file.getFileSize())).append("\n");
                
                if (file.getDescription() != null && !file.getDescription().isEmpty()) {
                    analysisResult.append("Описание: ").append(file.getDescription()).append("\n");
                }
                
                if (file.getTags() != null && !file.getTags().isEmpty()) {
                    analysisResult.append("Теги: ").append(file.getTags()).append("\n");
                }
                
                // Анализируем содержимое файла
                String contentAnalysis = analyzeFileContent(file);
                analysisResult.append("Анализ содержимого:\n").append(contentAnalysis).append("\n\n");
                
            } catch (Exception e) {
                log.error("Error analyzing file: {}", fileId, e);
                analysisResult.append("Ошибка анализа файла: ").append(e.getMessage()).append("\n\n");
            }
        }
        
        // Генерируем общие рекомендации
        analysisResult.append("=== РЕКОМЕНДАЦИИ ===\n");
        analysisResult.append(generateRecommendations(user, fileIds));
        
        return analysisResult.toString();
    }
    
    private String analyzeFileContent(FileUpload file) {
        try {
            Path filePath = Paths.get(file.getFilePath());
            
            if (!Files.exists(filePath)) {
                return "Файл не найден на диске";
            }
            
            String mimeType = file.getMimeType();
            
            if (mimeType.startsWith("image/")) {
                return analyzeImageFile(file);
            } else if (mimeType.equals("application/pdf")) {
                return analyzePdfFile(file);
            } else if (mimeType.startsWith("text/")) {
                return analyzeTextFile(file);
            } else if (mimeType.contains("word") || mimeType.contains("document")) {
                return analyzeWordFile(file);
            } else {
                return "Тип файла не поддерживается для анализа";
            }
            
        } catch (Exception e) {
            log.error("Error analyzing file content: {}", file.getOriginalFileName(), e);
            return "Ошибка при анализе файла: " + e.getMessage();
        }
    }
    
    private String analyzeImageFile(FileUpload file) {
        return String.format("""
            Изображение: %s
            - Размер: %s
            - Тип: %s
            - Рекомендация: Изображение может содержать важную визуальную информацию.
            - Примечание: Для полного анализа изображений требуется OCR технология.
            """, 
            file.getOriginalFileName(),
            formatFileSize(file.getFileSize()),
            file.getMimeType()
        );
    }
    
    private String analyzePdfFile(FileUpload file) {
        return String.format("""
            PDF документ: %s
            - Размер: %s
            - Рекомендация: PDF может содержать резюме, сертификаты или портфолио.
            - Примечание: Для извлечения текста из PDF требуется специальная обработка.
            """, 
            file.getOriginalFileName(),
            formatFileSize(file.getFileSize())
        );
    }
    
    private String analyzeTextFile(FileUpload file) {
        try {
            Path filePath = Paths.get(file.getFilePath());
            String content = Files.readString(filePath);
            
            return String.format("""
                Текстовый файл: %s
                - Размер: %s
                - Содержимое (первые 200 символов): %s
                - Рекомендация: Текстовый файл может содержать важную информацию о навыках и опыте.
                """, 
                file.getOriginalFileName(),
                formatFileSize(file.getFileSize()),
                content.length() > 200 ? content.substring(0, 200) + "..." : content
            );
        } catch (IOException e) {
            return "Ошибка чтения текстового файла: " + e.getMessage();
        }
    }
    
    private String analyzeWordFile(FileUpload file) {
        return String.format("""
            Документ Word: %s
            - Размер: %s
            - Рекомендация: Word документ может содержать резюме, портфолио или проекты.
            - Примечание: Для извлечения текста из Word требуется специальная обработка.
            """, 
            file.getOriginalFileName(),
            formatFileSize(file.getFileSize())
        );
    }
    
    private String generateRecommendations(User user, List<UUID> fileIds) {
        StringBuilder recommendations = new StringBuilder();
        
        recommendations.append("На основе загруженных файлов:\n\n");
        
        // Анализируем типы файлов
        long imageCount = fileIds.stream()
            .map(id -> fileUploadRepository.findById(id).orElse(null))
            .filter(file -> file != null && file.getMimeType().startsWith("image/"))
            .count();
            
        long pdfCount = fileIds.stream()
            .map(id -> fileUploadRepository.findById(id).orElse(null))
            .filter(file -> file != null && file.getMimeType().equals("application/pdf"))
            .count();
            
        long textCount = fileIds.stream()
            .map(id -> fileUploadRepository.findById(id).orElse(null))
            .filter(file -> file != null && file.getMimeType().startsWith("text/"))
            .count();
        
        recommendations.append("Статистика файлов:\n");
        recommendations.append("- Изображений: ").append(imageCount).append("\n");
        recommendations.append("- PDF документов: ").append(pdfCount).append("\n");
        recommendations.append("- Текстовых файлов: ").append(textCount).append("\n\n");
        
        recommendations.append("Рекомендации по развитию:\n");
        recommendations.append("1. Создайте структурированное портфолио\n");
        recommendations.append("2. Добавьте сертификаты и достижения\n");
        recommendations.append("3. Опишите ключевые проекты и результаты\n");
        recommendations.append("4. Укажите навыки и технологии\n");
        recommendations.append("5. Добавьте рекомендации от коллег\n\n");
        
        recommendations.append("Следующие шаги:\n");
        recommendations.append("- Заполните профиль на платформе\n");
        recommendations.append("- Укажите карьерные цели\n");
        recommendations.append("- Добавьте навыки и компетенции\n");
        recommendations.append("- Создайте план развития\n");
        
        return recommendations.toString();
    }
    
    public String getFileAnalysis(User user, UUID fileId) {
        FileUpload file = fileUploadRepository.findById(fileId).orElse(null);
        if (file == null || !file.getUser().getId().equals(user.getId())) {
            return null;
        }
        
        return analyzeFileContent(file);
    }
    
    private String formatFileSize(long bytes) {
        if (bytes == 0) return "0 Bytes";
        int k = 1024;
        String[] sizes = {"Bytes", "KB", "MB", "GB"};
        int i = (int) Math.floor(Math.log(bytes) / Math.log(k));
        return String.format("%.2f %s", bytes / Math.pow(k, i), sizes[i]);
    }
}
