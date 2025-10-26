package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.ProfileSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProfileSnapshotRepository {

    @Autowired
    private SupabaseProfileSnapshotRepository supabaseRepository;

    public List<ProfileSnapshot> findAll() {
        return supabaseRepository.findAll().block();
    }

    public Optional<ProfileSnapshot> findById(UUID id) {
        return supabaseRepository.findById(id).block();
    }

    public List<ProfileSnapshot> findLatestSnapshotsByUserId(UUID userId) {
        return supabaseRepository.findLatestSnapshotsByUserId(userId).block();
    }

    public List<ProfileSnapshot> findByUserIdOrderBySnapshotDateDesc(UUID userId) {
        return supabaseRepository.findByUserIdOrderBySnapshotDateDesc(userId).block();
    }

    public List<ProfileSnapshot> findSnapshotsBetweenDates(OffsetDateTime startDate, OffsetDateTime endDate) {
        return supabaseRepository.findSnapshotsBetweenDates(startDate, endDate).block();
    }

    public ProfileSnapshot save(ProfileSnapshot snapshot) {
        return supabaseRepository.save(snapshot).block();
    }

    public ProfileSnapshot update(ProfileSnapshot snapshot) {
        return supabaseRepository.update(snapshot).block();
    }

    public void deleteById(UUID id) {
        supabaseRepository.deleteById(id).block();
    }

    public long count() {
        return supabaseRepository.count().block();
    }
}

