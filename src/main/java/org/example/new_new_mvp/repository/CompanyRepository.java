package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    
    Optional<Company> findByName(String name);
    
    boolean existsByName(String name);
}
