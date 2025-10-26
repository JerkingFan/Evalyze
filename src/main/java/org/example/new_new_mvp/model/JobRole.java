package org.example.new_new_mvp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobRole {
    
    private UUID id;
    
    @JsonProperty("company_id")
    private UUID companyId;
    
    @JsonProperty("role_type")
    private String roleType; // ROLE, VACANCY, TEMPLATE
    
    private String title;
    
    private String description;
    
    private Object requirements; // JSONB
    
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
    
    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
}
