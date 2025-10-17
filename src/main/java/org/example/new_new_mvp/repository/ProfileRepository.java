package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.Profile;
import org.example.new_new_mvp.model.ProfileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    
    Optional<Profile> findByUserId(UUID userId);
    
    List<Profile> findByStatus(ProfileStatus status);
    
    List<Profile> findByUserCompanyId(UUID companyId);
    
    List<Profile> findByUserCompanyIdAndStatus(UUID companyId, ProfileStatus status);
}
