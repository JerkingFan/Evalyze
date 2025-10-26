package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepository {

    @Autowired
    private SupabaseUserRepository supabaseRepository;

    public Optional<User> findByEmail(String email) {
        return supabaseRepository.findByEmail(email).block();
    }

    public boolean existsByEmail(String email) {
        return supabaseRepository.existsByEmail(email).block();
    }
    
    public Optional<User> findByActivationCode(String activationCode) {
        return supabaseRepository.findByActivationCode(activationCode).block();
    }
    
    public boolean existsByActivationCode(String activationCode) {
        return supabaseRepository.existsByActivationCode(activationCode).block();
    }

    public List<User> findByRole(UserRole role) {
        return supabaseRepository.findByRole(role).block();
    }

    public List<User> findByCompanyId(UUID companyId) {
        return supabaseRepository.findByCompanyId(companyId).block();
    }

    public List<User> findByCompanyIdAndRole(UUID companyId, UserRole role) {
        return supabaseRepository.findByCompanyIdAndRole(companyId, role).block();
    }

    public long countByCompanyId(UUID companyId) {
        return supabaseRepository.countByCompanyId(companyId).block();
    }

    public long countByRole(UserRole role) {
        return supabaseRepository.countByRole(role).block();
    }

    public User save(User user) {
        System.out.println("UserRepository.save() - blocking call to supabaseRepository");
        try {
            User savedUser = supabaseRepository.save(user).block();
            System.out.println("UserRepository.save() - SUCCESS: " + (savedUser != null ? savedUser.getEmail() : "null"));
            return savedUser;
        } catch (Exception e) {
            System.out.println("UserRepository.save() - ERROR: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public Optional<User> findById(UUID id) {
        return supabaseRepository.findById(id).block();
    }

    public void deleteById(UUID id) {
        supabaseRepository.deleteById(id).block();
    }

    public List<User> findAll() {
        return supabaseRepository.findAll().block();
    }

    public long count() {
        return supabaseRepository.count().block();
    }

    public boolean updateUserRoleByEmail(String email, java.util.UUID roleId) {
        return supabaseRepository.updateUserRoleByEmail(email, roleId).blockOptional().orElse(false);
    }

    public boolean updateUserRoleByActivationCode(String activationCode, java.util.UUID roleId) {
        return supabaseRepository.updateUserRoleByActivationCode(activationCode, roleId).blockOptional().orElse(false);
    }
}

