package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.CompanyContent;
import org.example.new_new_mvp.model.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CompanyContentRepository {

    @Autowired
    private SupabaseCompanyContentRepository supabaseRepository;

    public List<CompanyContent> findAll() {
        return supabaseRepository.findAll().block();
    }

    public Optional<CompanyContent> findById(UUID id) {
        return supabaseRepository.findById(id).block();
    }

    public List<CompanyContent> findByCompanyId(UUID companyId) {
        return supabaseRepository.findByCompanyId(companyId).block();
    }

    public List<CompanyContent> findByContentType(ContentType contentType) {
        return supabaseRepository.findByContentType(contentType).block();
    }

    public List<CompanyContent> findByCompanyIdAndContentType(UUID companyId, ContentType contentType) {
        return supabaseRepository.findByCompanyIdAndContentType(companyId, contentType).block();
    }

    public List<CompanyContent> findByCompanyIdAndTitleContainingIgnoreCase(UUID companyId, String searchTerm) {
        return supabaseRepository.findByCompanyIdAndTitleContainingIgnoreCase(companyId, searchTerm).block();
    }

    public CompanyContent save(CompanyContent content) {
        return supabaseRepository.save(content).block();
    }

    public CompanyContent update(CompanyContent content) {
        return supabaseRepository.update(content).block();
    }

    public void deleteById(UUID id) {
        supabaseRepository.deleteById(id).block();
    }

    public long count() {
        return supabaseRepository.count().block();
    }

    public long countByCompanyId(UUID companyId) {
        return supabaseRepository.countByCompanyId(companyId).block();
    }
}

