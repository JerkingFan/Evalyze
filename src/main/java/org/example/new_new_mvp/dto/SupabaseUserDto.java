package org.example.new_new_mvp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO для маппинга с существующей таблицей users в Supabase
 * Структура таблицы:
 * - id (bigint, auto-increment)
 * - email, full_name
 * - status (text) - храним role и company_id как JSON
 * - access_token, refresh_token, token_expires_at
 * - telegram_chat_id, activation_code
 * - tracked_folders, temp_selected_folders, temp_full_folder_list (jsonb)
 * - Skills (jsonb)
 * - created_at, last_updated
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Игнорируем неизвестные поля из Supabase
public class SupabaseUserDto {
    
    private Long id; // bigint в Supabase
    
    private String email;
    
    @JsonProperty("full_name")
    private String fullName;
    
    // Храним role и company_id как JSON строку
    private String status;
    
    private String password;
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("token_expires_at")
    private OffsetDateTime tokenExpiresAt; // timestamp with time zone
    
    @JsonProperty("telegram_chat_id")
    private String telegramChatId;
    
    @JsonProperty("activation_code")
    private String activationCode;
    
    @JsonProperty("tracked_folders")
    private Object trackedFolders; // jsonb
    
    @JsonProperty("temp_selected_folders")
    private Object tempSelectedFolders; // jsonb
    
    @JsonProperty("temp_full_folder_list")
    private Object tempFullFolderList; // jsonb
    
    @JsonProperty("Skills")
    private Object skills; // jsonb
    
    @JsonProperty("created_at")
    private OffsetDateTime createdAt; // timestamp with time zone
    
    @JsonProperty("last_updated")
    private OffsetDateTime lastUpdated; // timestamp with time zone
}

