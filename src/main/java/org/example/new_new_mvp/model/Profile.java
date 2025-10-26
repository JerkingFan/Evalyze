package org.example.new_new_mvp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Profile {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Id
    @Column(name = "user_id")
    @JsonProperty("user_id")
    private UUID userId;
    
    // Hidden field for JPA persistence
    @Column(name = "profile_data", columnDefinition = "jsonb")
    @JsonIgnore
    private String profileDataJson;
    
    // Transient field that converts to/from JsonNode
    @Transient
    @JsonProperty("profile_data")
    private JsonNode profileData;
    
    @Column(name = "company_id")
    @JsonProperty("company_id")
    private UUID companyId;
    
    @Column(name = "last_updated")
    @JsonProperty("last_updated")
    private OffsetDateTime lastUpdated;
    
    // Non-persistent field for backward compatibility
    @Transient
    private ProfileStatus status;
    
    // Custom getters and setters for profileData
    @PostLoad
    private void deserializeProfileData() {
        if (profileDataJson != null && !profileDataJson.isEmpty()) {
            try {
                this.profileData = objectMapper.readTree(profileDataJson);
            } catch (Exception e) {
                this.profileData = null;
            }
        }
    }
    
    @PrePersist
    @PreUpdate
    private void serializeProfileData() {
        if (profileData != null) {
            try {
                this.profileDataJson = objectMapper.writeValueAsString(profileData);
            } catch (Exception e) {
                this.profileDataJson = "{}";
            }
        }
    }
    
    // Setter/getter for backward compatibility
    public ProfileStatus getStatus() {
        // We can derive status from profile_data if needed
        return status != null ? status : ProfileStatus.COMPLETED;
    }
    
    public void setStatus(ProfileStatus status) {
        this.status = status;
    }
}