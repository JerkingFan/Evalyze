package org.example.new_new_mvp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    private String id;
    private String name;
    private OffsetDateTime createdAt;
    private long userCount;
    private long invitationCount;
    
    /**
     * Set ID from UUID
     */
    public void setId(UUID uuid) {
        this.id = uuid != null ? uuid.toString() : null;
    }
    
    /**
     * Get ID as UUID
     */
    public UUID getIdAsUuid() {
        return id != null ? UUID.fromString(id) : null;
    }
}
