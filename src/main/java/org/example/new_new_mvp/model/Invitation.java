package org.example.new_new_mvp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invitation {
    
    private UUID id;
    
    private Company company;
    
    private String email;
    
    private String invitationCode;
    
    private InvitationStatus status;
    
    private LocalDateTime expiresAt;
    
    /**
     * Проверить, действительна ли приглашение
     */
    public boolean isValid() {
        return status == InvitationStatus.PENDING && !isExpired();
    }
    
    /**
     * Проверить, истекло ли приглашение
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}