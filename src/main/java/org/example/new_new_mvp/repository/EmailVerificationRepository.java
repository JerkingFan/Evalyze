package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {
    
    Optional<EmailVerification> findByEmailAndVerificationCodeAndIsUsedFalse(String email, String verificationCode);
    
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.email = :email AND ev.isUsed = false AND ev.expiresAt > :now ORDER BY ev.createdAt DESC")
    Optional<EmailVerification> findLatestActiveByEmail(@Param("email") String email, @Param("now") LocalDateTime now);
    
    @Modifying
    @Transactional
    @Query("UPDATE EmailVerification ev SET ev.isUsed = true WHERE ev.email = :email AND ev.isUsed = false")
    void markAllAsUsedByEmail(@Param("email") String email);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerification ev WHERE ev.expiresAt < :now")
    void deleteExpiredVerifications(@Param("now") LocalDateTime now);
}
