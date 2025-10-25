package org.example.new_new_mvp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.new_new_mvp.model.UserRole;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    
    private UUID id;
    
    private String email;
    
    @JsonProperty("full_name")
    private String fullName;
    
    private UserRole role;
    
    @JsonProperty("company_id")
    private UUID companyId;
    
    @JsonProperty("google_oauth_token")
    private String googleOauthToken;
}
