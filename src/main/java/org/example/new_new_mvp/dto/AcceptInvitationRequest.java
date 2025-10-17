package org.example.new_new_mvp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcceptInvitationRequest {
    
    @NotBlank(message = "Invitation code is required")
    private String invitationCode;
    
    @NotBlank(message = "Full name is required")
    private String fullName;
}
