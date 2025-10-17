package org.example.new_new_mvp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.new_new_mvp.model.InvitationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationDto {
    private UUID id;
    private UUID companyId;
    private String companyName;
    private String email;
    private String invitationCode;
    private InvitationStatus status;
    private LocalDateTime expiresAt;
    private boolean expired;
}
