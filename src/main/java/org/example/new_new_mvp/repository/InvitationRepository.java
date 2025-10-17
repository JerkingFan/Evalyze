package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.Invitation;
import org.example.new_new_mvp.model.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {
    
    Optional<Invitation> findByInvitationCode(String invitationCode);
    
    Optional<Invitation> findByEmailAndCompanyId(String email, UUID companyId);
    
    List<Invitation> findByCompanyId(UUID companyId);
    
    List<Invitation> findByCompanyIdAndStatus(UUID companyId, InvitationStatus status);
    
    List<Invitation> findByStatus(InvitationStatus status);
    
    @Query("SELECT i FROM Invitation i WHERE i.expiresAt < :now AND i.status = :status")
    List<Invitation> findExpiredInvitations(@Param("now") LocalDateTime now, @Param("status") InvitationStatus status);
    
    boolean existsByEmailAndCompanyId(String email, UUID companyId);
    
    long countByCompanyId(UUID companyId);
}
