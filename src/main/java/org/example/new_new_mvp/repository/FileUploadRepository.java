package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.FileUpload;
import org.example.new_new_mvp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, UUID> {
    
    List<FileUpload> findByUserOrderByUploadedAtDesc(User user);
    
    @Query("SELECT f FROM FileUpload f WHERE f.user = :user AND f.originalFileName LIKE %:searchTerm%")
    List<FileUpload> findByUserAndOriginalFileNameContaining(@Param("user") User user, @Param("searchTerm") String searchTerm);
    
    @Query("SELECT f FROM FileUpload f WHERE f.user = :user AND f.mimeType LIKE %:mimeType%")
    List<FileUpload> findByUserAndMimeTypeContaining(@Param("user") User user, @Param("mimeType") String mimeType);
    
    void deleteByUser(User user);
}
