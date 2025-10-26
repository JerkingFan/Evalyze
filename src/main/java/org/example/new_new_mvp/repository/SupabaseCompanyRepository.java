package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.dto.CompanyInsertDto;
import org.example.new_new_mvp.model.Company;
import org.example.new_new_mvp.service.SupabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SupabaseCompanyRepository {

    @Autowired
    private SupabaseService supabaseService;

    /**
     * Получить все компании
     */
    public Mono<List<Company>> findAll() {
        return supabaseService.select("companies", Company.class);
    }

    /**
     * Найти компанию по ID
     */
    public Mono<Optional<Company>> findById(UUID id) {
        Map<String, String> filters = Map.of("id", "eq." + id.toString());
        return supabaseService.select("companies", Company.class, filters)
                .map(companies -> companies.isEmpty() ? Optional.empty() : Optional.of(companies.get(0)));
    }

    /**
     * Найти компанию по имени
     */
    public Mono<Optional<Company>> findByName(String name) {
        Map<String, String> filters = Map.of("name", "eq." + name);
        return supabaseService.select("companies", Company.class, filters)
                .map(companies -> companies.isEmpty() ? Optional.empty() : Optional.of(companies.get(0)));
    }

    /**
     * Сохранить компанию
     */
    public Mono<Company> save(Company company) {
        // Создаем DTO без связей для Supabase
        CompanyInsertDto dto = new CompanyInsertDto();
        // НЕ устанавливаем id - Supabase сгенерирует его автоматически через gen_random_uuid()
        // dto.setId(company.getId());
        dto.setName(company.getName());
        
        // created_at будет установлен автоматически в Supabase
        
        System.out.println("Saving company: " + dto.getName() + " (id will be auto-generated)");
        
        return supabaseService.insert("companies", dto, CompanyInsertDto.class)
                .map(savedDto -> {
                    // Преобразуем обратно в Company
                    Company savedCompany = new Company();
                    savedCompany.setId(savedDto.getId());
                    savedCompany.setName(savedDto.getName());
                    savedCompany.setCreatedAt(java.time.OffsetDateTime.now());
                    return savedCompany;
                })
                .doOnError(error -> System.out.println("Error saving company: " + error.getMessage()));
    }

    /**
     * Обновить компанию
     */
    public Mono<Company> update(Company company) {
        Map<String, String> filters = Map.of("id", "eq." + company.getId().toString());
        return supabaseService.update("companies", company, filters)
                .cast(Company.class);
    }

    /**
     * Удалить компанию
     */
    public Mono<Void> deleteById(UUID id) {
        Map<String, String> filters = Map.of("id", "eq." + id.toString());
        return supabaseService.delete("companies", filters);
    }

    /**
     * Проверить существование компании
     */
    public Mono<Boolean> existsById(UUID id) {
        Map<String, String> filters = Map.of("id", "eq." + id.toString());
        return supabaseService.select("companies", Company.class, filters)
                .map(companies -> !companies.isEmpty());
    }

    /**
     * Проверить существование компании по имени
     */
    public Mono<Boolean> existsByName(String name) {
        Map<String, String> filters = Map.of("name", "eq." + name);
        return supabaseService.select("companies", Company.class, filters)
                .map(companies -> !companies.isEmpty());
    }

    /**
     * Подсчитать все компании
     */
    public Mono<Long> count() {
        return supabaseService.select("companies", Company.class)
                .map(List::size)
                .map(Long::valueOf);
    }
}

