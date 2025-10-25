package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.Invitation;
import org.example.new_new_mvp.model.InvitationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InvitationRepository {

    @Autowired
    private SupabaseInvitationRepository supabaseRepository;

    public List<Invitation> findAll() {
        return supabaseRepository.findAll().block();
    }

    public Optional<Invitation> findById(UUID id) {
        return supabaseRepository.findById(id).block();
    }

    public Optional<Invitation> findByEmail(String email) {
        return supabaseRepository.findByEmail(email).block();
    }

    public Optional<Invitation> findByInvitationCode(String invitationCode) {
        return supabaseRepository.findByInvitationCode(invitationCode).block();
    }

    public List<Invitation> findByStatus(InvitationStatus status) {
        return supabaseRepository.findByStatus(status).block();
    }

    public List<Invitation> findByCompanyId(UUID companyId) {
        return supabaseRepository.findByCompanyId(companyId).block();
    }

    public List<Invitation> findByCompanyIdAndStatus(UUID companyId, InvitationStatus status) {
        return supabaseRepository.findByCompanyIdAndStatus(companyId, status).block();
    }

    public long countByCompanyId(UUID companyId) {
        return supabaseRepository.countByCompanyId(companyId).block();
    }

    public long countByStatus(InvitationStatus status) {
        return supabaseRepository.countByStatus(status).block();
    }

    public Invitation save(Invitation invitation) {
        return supabaseRepository.save(invitation).block();
    }

    public Invitation update(Invitation invitation) {
        return supabaseRepository.update(invitation).block();
    }

    public void deleteById(UUID id) {
        supabaseRepository.deleteById(id).block();
    }

    public boolean existsById(UUID id) {
        return supabaseRepository.existsById(id).block();
    }

    public boolean existsByEmailAndCompanyId(String email, UUID companyId) {
        return supabaseRepository.existsByEmailAndCompanyId(email, companyId).block();
    }

    public long count() {
        return supabaseRepository.count().block();
    }
}

