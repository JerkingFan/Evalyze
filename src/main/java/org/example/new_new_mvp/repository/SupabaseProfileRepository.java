package org.example.new_new_mvp.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.new_new_mvp.dto.ProfileInsertDto;
import org.example.new_new_mvp.model.Profile;
import org.example.new_new_mvp.model.ProfileStatus;
import org.example.new_new_mvp.service.SupabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class SupabaseProfileRepository {

    @Autowired
    private SupabaseService supabaseService;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Получить все профили
     */
    public Mono<List<Profile>> findAll() {
        return supabaseService.select("profiles", Profile.class);
    }

    /**
     * Найти профиль по ID пользователя
     */
    public Mono<Optional<Profile>> findByUserId(UUID userId) {
        Map<String, String> filters = Map.of("user_id", "eq." + userId.toString());
        return supabaseService.select("profiles", Profile.class, filters)
                .map(profiles -> profiles.isEmpty() ? Optional.empty() : Optional.of(profiles.get(0)));
    }

    /**
     * Найти профили по статусу
     * Disabled: status field doesn't exist in Supabase schema
     */
    // public Mono<List<Profile>> findByStatus(ProfileStatus status) {
    //     Map<String, String> filters = Map.of("status", "eq." + status.name());
    //     return supabaseService.select("profiles", Profile.class, filters);
    // }

    /**
     * Найти профили по ID компании
     */
    public Mono<List<Profile>> findByUserCompanyId(UUID companyId) {
        // Note: This requires a join or nested query in Supabase
        // Temporarily disabled due to model changes
        return supabaseService.select("profiles", Profile.class)
                .map(profiles -> profiles.stream()
                        .filter(p -> p.getUserId() != null)
                        .collect(Collectors.toList()));
    }

    /**
     * Найти профили по ID компании и статусу
     * Disabled: status field doesn't exist in Supabase schema
     */
    // public Mono<List<Profile>> findByUserCompanyIdAndStatus(UUID companyId, ProfileStatus status) {
    //     return findByUserCompanyId(companyId)
    //             .map(profiles -> profiles.stream()
    //                     .filter(p -> status.equals(p.getStatus()))
    //                     .collect(Collectors.toList()));
    // }

    /**
     * Подсчитать профили по статусу
     * Disabled: status field doesn't exist in Supabase schema
     */
    // public Mono<Long> countByStatus(ProfileStatus status) {
    //     Map<String, String> filters = Map.of("status", "eq." + status.name());
    //     return supabaseService.select("profiles", Profile.class, filters)
    //             .map(List::size)
    //             .cast(Long.class);
    // }

    /**
     * Сохранить профиль
     */
    public Mono<Profile> save(Profile profile) {
        System.out.println("Saving profile: " + profile);
        
        // Check if profile already exists
        return findByUserId(profile.getUserId())
                .flatMap(existingProfile -> {
                    if (existingProfile.isPresent()) {
                        System.out.println("Profile exists, updating...");
                        return update(profile);
                    } else {
                        System.out.println("Profile does not exist, inserting...");
                        
                        // Convert Profile to ProfileInsertDto (without transient fields)
                        ProfileInsertDto insertDto = new ProfileInsertDto();
                        insertDto.setUserId(profile.getUserId());
                        
                        System.out.println("=== PROFILE INSERT DEBUG ===");
                        System.out.println("user_id: " + profile.getUserId());
                        System.out.println("user_id type: " + profile.getUserId().getClass().getName());
                        System.out.println("company_id: " + profile.getCompanyId());
                        System.out.println("profile_data: " + profile.getProfileData());
                        
                        // Convert profile data to JsonNode for Supabase
                        // The profile object has @PostLoad/@PrePersist handlers, but we need to manually trigger them for Supabase
                        if (profile.getProfileData() != null) {
                            try {
                                insertDto.setProfileData(objectMapper.valueToTree(profile.getProfileData()));
                            } catch (Exception e) {
                                insertDto.setProfileData(profile.getProfileData());
                            }
                        }
                        System.out.println("profile_data set successfully");
                        
                        insertDto.setCompanyId(profile.getCompanyId());
                        insertDto.setLastUpdated(profile.getLastUpdated());
                        
                        System.out.println("Inserting into profiles with DTO: " + insertDto);
                        System.out.println("============================");
                        
                        return supabaseService.insert("profiles", insertDto, ProfileInsertDto.class)
                                .map(savedDto -> {
                                    // Convert back to Profile
                                    Profile savedProfile = new Profile();
                                    savedProfile.setUserId(savedDto.getUserId());
                                    // profile_data is already JsonNode
                                    savedProfile.setProfileData(savedDto.getProfileData());
                                    savedProfile.setCompanyId(savedDto.getCompanyId());
                                    savedProfile.setLastUpdated(savedDto.getLastUpdated());
                                    savedProfile.setStatus(profile.getStatus()); // Set transient status
                                    return savedProfile;
                                });
                    }
                })
                .doOnError(error -> {
                    System.err.println("Error saving profile: " + error.getMessage());
                    error.printStackTrace();
                });
    }

    /**
     * Обновить профиль
     */
    public Mono<Profile> update(Profile profile) {
        // Convert Profile to ProfileInsertDto (without transient fields) for update
        ProfileInsertDto updateDto = new ProfileInsertDto();
        updateDto.setUserId(profile.getUserId());
        
        // Convert profile data to JsonNode for Supabase
        if (profile.getProfileData() != null) {
            try {
                updateDto.setProfileData(objectMapper.valueToTree(profile.getProfileData()));
            } catch (Exception e) {
                updateDto.setProfileData(profile.getProfileData());
            }
        }
        
        updateDto.setCompanyId(profile.getCompanyId());
        updateDto.setLastUpdated(profile.getLastUpdated());
        
        Map<String, String> filters = Map.of("user_id", "eq." + profile.getUserId().toString());
        return supabaseService.update("profiles", updateDto, filters)
                .cast(ProfileInsertDto.class)
                .map(savedDto -> {
                    // Convert back to Profile
                    Profile savedProfile = new Profile();
                    savedProfile.setUserId(savedDto.getUserId());
                    // profile_data is already JsonNode
                    savedProfile.setProfileData(savedDto.getProfileData());
                    savedProfile.setCompanyId(savedDto.getCompanyId());
                    savedProfile.setLastUpdated(savedDto.getLastUpdated());
                    savedProfile.setStatus(profile.getStatus()); // Set transient status
                    return savedProfile;
                });
    }

    /**
     * Удалить профиль по ID пользователя
     */
    public Mono<Void> deleteByUserId(UUID userId) {
        Map<String, String> filters = Map.of("user_id", "eq." + userId.toString());
        return supabaseService.delete("profiles", filters);
    }

    /**
     * Подсчитать все профили
     */
    public Mono<Long> count() {
        return supabaseService.select("profiles", Profile.class)
                .map(List::size)
                .cast(Long.class);
    }
}

