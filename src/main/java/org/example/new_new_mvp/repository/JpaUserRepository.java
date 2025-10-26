package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository для User - используется только для локальной БД (H2)
 * Для Supabase используется SupabaseUserRepository
 * 
 * ВАЖНО: company и role помечены как @Transient, поэтому методы с ними удалены
 */
@Repository
public interface JpaUserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    
    Optional<User> findByActivationCode(String activationCode);
    
    boolean existsByActivationCode(String activationCode);
    
    // Методы с company и role удалены, так как это @Transient поля
    // Используйте SupabaseUserRepository для работы с Supabase
}
