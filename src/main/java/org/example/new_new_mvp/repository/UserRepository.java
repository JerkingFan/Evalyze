package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(UserRole role);
    
    List<User> findByCompanyId(UUID companyId);
    
    List<User> findByCompanyIdAndRole(UUID companyId, UserRole role);
    
    long countByCompanyId(UUID companyId);
}