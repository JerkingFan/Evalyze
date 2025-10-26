package org.example.new_new_mvp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobRoleDto {
    private UUID id;
    private UUID companyId;
    private String roleType;
    private String title;
    private String description;
    private String requirements;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
