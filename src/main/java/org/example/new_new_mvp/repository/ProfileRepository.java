package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.Profile;
import org.example.new_new_mvp.model.ProfileStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProfileRepository {

    @Autowired
    private SupabaseProfileRepository supabaseRepository;

    public List<Profile> findAll() {
        return supabaseRepository.findAll().block();
    }

    public Optional<Profile> findByUserId(UUID userId) {
        return supabaseRepository.findByUserId(userId).block();
    }

    public List<Profile> findByStatus(ProfileStatus status) {
        return supabaseRepository.findByStatus(status).block();
    }

    public List<Profile> findByUserCompanyId(UUID companyId) {
        return supabaseRepository.findByUserCompanyId(companyId).block();
    }

    public List<Profile> findByUserCompanyIdAndStatus(UUID companyId, ProfileStatus status) {
        return supabaseRepository.findByUserCompanyIdAndStatus(companyId, status).block();
    }

    public long countByStatus(ProfileStatus status) {
        return supabaseRepository.countByStatus(status).block();
    }

    public Profile save(Profile profile) {
        return supabaseRepository.save(profile).block();
    }

    public Profile update(Profile profile) {
        return supabaseRepository.update(profile).block();
    }

    public void deleteByUserId(UUID userId) {
        supabaseRepository.deleteByUserId(userId).block();
    }

    public long count() {
        return supabaseRepository.count().block();
    }
}

