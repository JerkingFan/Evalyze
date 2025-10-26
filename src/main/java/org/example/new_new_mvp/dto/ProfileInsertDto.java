package org.example.new_new_mvp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for inserting Profile data into Supabase
 * Does not include the transient 'status' field
 * profile_data is JsonNode because Supabase expects jsonb type
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileInsertDto {
    
    @JsonProperty("user_id")
    private UUID userId;
    
    @JsonProperty("profile_data")
    private JsonNode profileData;  // Use JsonNode for jsonb fields
    
    @JsonProperty("company_id")
    private UUID companyId;
    
    @JsonProperty("last_updated")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime lastUpdated;
}
