package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.CompanyContent;
import org.example.new_new_mvp.model.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyContentRepository extends JpaRepository<CompanyContent, UUID> {
    
    List<CompanyContent> findByCompanyId(UUID companyId);
    
    List<CompanyContent> findByCompanyIdAndContentType(UUID companyId, ContentType contentType);
    
    List<CompanyContent> findByContentType(ContentType contentType);
    
    @Query("SELECT cc FROM CompanyContent cc WHERE cc.company.id = :companyId AND LOWER(cc.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<CompanyContent> findByCompanyIdAndTitleContainingIgnoreCase(@Param("companyId") UUID companyId, @Param("searchTerm") String searchTerm);
}
