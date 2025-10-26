package org.example.new_new_mvp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.new_new_mvp.model.FileUpload;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    
    private UUID id;
    private String originalFileName;
    private String storedFileName;
    private String filePath;
    private Long fileSize;
    private String mimeType;
    private LocalDateTime uploadedAt;
    private String description;
    private String tags;
    
    public static FileUploadResponse fromFileUpload(FileUpload fileUpload) {
        return new FileUploadResponse(
            fileUpload.getId(),
            fileUpload.getOriginalFileName(),
            fileUpload.getStoredFileName(),
            fileUpload.getFilePath(),
            fileUpload.getFileSize(),
            fileUpload.getMimeType(),
            fileUpload.getUploadedAt(),
            fileUpload.getDescription(),
            fileUpload.getTags()
        );
    }
}
