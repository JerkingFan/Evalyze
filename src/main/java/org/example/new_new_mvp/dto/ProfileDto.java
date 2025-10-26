package org.example.new_new_mvp.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.new_new_mvp.model.ProfileStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {
    private UUID userId;
    private String userEmail;
    private String userFullName;
    private JsonNode profileData;
    private ProfileStatus status;
    private OffsetDateTime lastUpdated;
}
