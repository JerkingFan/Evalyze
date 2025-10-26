package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaProfileRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findByUserId(UUID userId);

    // Removed methods that use 'status' field since it's @Transient
    // List<Profile> findByStatus(ProfileStatus status);
    // long countByStatus(ProfileStatus status);
}
