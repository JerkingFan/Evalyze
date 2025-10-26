package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.CompanyContent;
import org.example.new_new_mvp.model.ContentType;
import org.example.new_new_mvp.service.SupabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class SupabaseCompanyContentRepository {

    @Autowired
    private SupabaseService supabaseService;

    /**
     * Получить весь контент
     */
    public Mono<List<CompanyContent>> findAll() {
        return supabaseService.select("company_content", CompanyContent.class);
    }

    /**
     * Найти контент по ID
     */
    public Mono<Optional<CompanyContent>> findById(UUID id) {
        Map<String, String> filters = Map.of("id", "eq." + id.toString());
        return supabaseService.select("company_content", CompanyContent.class, filters)
                .map(content -> content.isEmpty() ? Optional.empty() : Optional.of(content.get(0)));
    }

    /**
     * Найти контент по ID компании
     */
    public Mono<List<CompanyContent>> findByCompanyId(UUID companyId) {
        Map<String, String> filters = Map.of("company_id", "eq." + companyId.toString());
        return supabaseService.select("company_content", CompanyContent.class, filters);
    }

    /**
     * Найти контент по типу
     */
    public Mono<List<CompanyContent>> findByContentType(ContentType contentType) {
        Map<String, String> filters = Map.of("content_type", "eq." + contentType.name());
        return supabaseService.select("company_content", CompanyContent.class, filters);
    }

    /**
     * Найти контент по ID компании и типу
     */
    public Mono<List<CompanyContent>> findByCompanyIdAndContentType(UUID companyId, ContentType contentType) {
        Map<String, String> filters = Map.of(
            "company_id", "eq." + companyId.toString(),
            "content_type", "eq." + contentType.name()
        );
        return supabaseService.select("company_content", CompanyContent.class, filters);
    }

    /**
     * Найти контент по ID компании и поиску в заголовке
     */
    public Mono<List<CompanyContent>> findByCompanyIdAndTitleContainingIgnoreCase(UUID companyId, String searchTerm) {
        Map<String, String> filters = Map.of("company_id", "eq." + companyId.toString());
        return supabaseService.select("company_content", CompanyContent.class, filters)
                .map(content -> content.stream()
                        .filter(c -> c.getTitle() != null && 
                                c.getTitle().toLowerCase().contains(searchTerm.toLowerCase()))
                        .collect(Collectors.toList()));
    }

    /**
     * Сохранить контент
     */
    public Mono<CompanyContent> save(CompanyContent content) {
        return supabaseService.insert("company_content", content, CompanyContent.class)
                .cast(CompanyContent.class);
    }

    /**
     * Обновить контент
     */
    public Mono<CompanyContent> update(CompanyContent content) {
        Map<String, String> filters = Map.of("id", "eq." + content.getId().toString());
        return supabaseService.update("company_content", content, filters)
                .cast(CompanyContent.class);
    }

    /**
     * Удалить контент
     */
    public Mono<Void> deleteById(UUID id) {
        Map<String, String> filters = Map.of("id", "eq." + id.toString());
        return supabaseService.delete("company_content", filters);
    }

    /**
     * Подсчитать весь контент
     */
    public Mono<Long> count() {
        return supabaseService.select("company_content", CompanyContent.class)
                .map(List::size)
                .cast(Long.class);
    }

    /**
     * Подсчитать контент по ID компании
     */
    public Mono<Long> countByCompanyId(UUID companyId) {
        Map<String, String> filters = Map.of("company_id", "eq." + companyId.toString());
        return supabaseService.select("company_content", CompanyContent.class, filters)
                .map(List::size)
                .cast(Long.class);
    }
}

