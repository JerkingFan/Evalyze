package org.example.new_new_mvp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.new_new_mvp.model.UserRole;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private UUID companyId;
    private String companyName;
    private String email;
    private String fullName;
    private UserRole role;
    private String profileStatus;
    private boolean hasProfile;
}
