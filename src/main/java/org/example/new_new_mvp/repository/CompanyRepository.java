package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CompanyRepository {

    @Autowired
    private SupabaseCompanyRepository supabaseRepository;

    public List<Company> findAll() {
        return supabaseRepository.findAll().block();
    }

    public Optional<Company> findById(UUID id) {
        return supabaseRepository.findById(id).block();
    }

    public Optional<Company> findByName(String name) {
        return supabaseRepository.findByName(name).block();
    }

    public Company save(Company company) {
        return supabaseRepository.save(company).block();
    }

    public Company update(Company company) {
        return supabaseRepository.update(company).block();
    }

    public void deleteById(UUID id) {
        supabaseRepository.deleteById(id).block();
    }

    public boolean existsById(UUID id) {
        return supabaseRepository.existsById(id).block();
    }

    public boolean existsByName(String name) {
        return supabaseRepository.existsByName(name).block();
    }

    public long count() {
        return supabaseRepository.count().block();
    }
}

