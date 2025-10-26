package org.example.new_new_mvp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.new_new_mvp.dto.ProfileDto;
import org.example.new_new_mvp.dto.CreateProfileRequest;
import org.example.new_new_mvp.model.Profile;
import org.example.new_new_mvp.model.ProfileSnapshot;
import org.example.new_new_mvp.model.ProfileStatus;
import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.model.UserRole;
import org.example.new_new_mvp.model.Company;
import org.example.new_new_mvp.model.JobRole;
import org.example.new_new_mvp.repository.ProfileRepository;
import org.example.new_new_mvp.repository.ProfileSnapshotRepository;
import org.example.new_new_mvp.repository.UserRepository;
import org.example.new_new_mvp.repository.CompanyRepository;
import org.example.new_new_mvp.repository.JobRoleRepositoryAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProfileService {
    
    @Autowired
    private ProfileRepository profileRepository;
    
    @Autowired
    private ProfileSnapshotRepository profileSnapshotRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private JobRoleRepositoryAdapter jobRoleRepository;
    
    @Autowired
    private WebhookService webhookService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public ProfileDto createOrUpdateProfile(UUID userId, String profileData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Profile profile = profileRepository.findByUserId(userId).orElse(new Profile());
        profile.setUserId(userId);
        try {
            profile.setProfileData(objectMapper.readTree(profileData));
        } catch (Exception e) {
            throw new RuntimeException("Error parsing profile data", e);
        }
        profile.setStatus(ProfileStatus.PENDING);
        profile.setLastUpdated(java.time.OffsetDateTime.now());
        
        Profile savedProfile = profileRepository.save(profile);
        
        // Create snapshot
        createSnapshot(userId, profileData);
        
        return convertToDto(savedProfile, user);
    }
    
    public Optional<ProfileDto> getProfileByUserId(UUID userId) {
        return profileRepository.findByUserId(userId)
                .map(this::convertToDto);
    }
    
    public List<ProfileDto> getCompanyProfiles(UUID companyId) {
        return profileRepository.findByUserCompanyId(companyId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<ProfileDto> getProfilesByStatus(ProfileStatus status) {
        // Status field doesn't exist in Supabase schema, return all profiles
        return profileRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public ProfileDto updateProfileStatus(UUID userId, ProfileStatus status) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        
        profile.setStatus(status);
        Profile savedProfile = profileRepository.save(profile);
        
        return convertToDto(savedProfile);
    }
    
    public void createSnapshot(UUID userId, String profileData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        ProfileSnapshot snapshot = new ProfileSnapshot();
        snapshot.setUserId(userId);
        snapshot.setProfileData(profileData);
        snapshot.setSnapshotDate(java.time.OffsetDateTime.now());
        
        profileSnapshotRepository.save(snapshot);
    }
    
    public List<ProfileSnapshot> getUserSnapshots(UUID userId) {
        return profileSnapshotRepository.findByUserIdOrderBySnapshotDateDesc(userId);
    }
    
    public List<?> getAllProfiles() {
        List<Profile> profiles = profileRepository.findAll();
        return profiles.stream()
                .map(this::convertToSimpleDto)
                .collect(Collectors.toList());
    }
    
    private Object convertToSimpleDto(Profile profile) {
        // Parse profile data if it's a string
        Object profileData = null;
        if (profile.getProfileData() != null) {
            try {
                profileData = new Object() {
                    // We'll parse this in JavaScript
                };
            } catch (Exception e) {
                profileData = profile.getProfileData();
            }
        }
        
        // Get user information separately
        final User user = userRepository.findById(profile.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return new Object() {
            public final UUID userId = profile.getUserId();
            public final String employeeEmail = user.getEmail();
            public final String employeeName = user.getFullName();
            public final String profileData = profile.getProfileData() != null ? profile.getProfileData().toString() : "{}";
            public final ProfileStatus status = profile.getStatus();
            public final boolean isVerified = profile.getStatus() == ProfileStatus.COMPLETED;
            public final boolean aiProfileGenerated = false; // For now, always false
            public final OffsetDateTime lastUpdated = profile.getLastUpdated();
        };
    }
    
    public Object findProfileByEmail(String email) {
        System.out.println("Searching for profile with email: " + email);
        
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElse(null);
        
        if (user == null) {
            System.out.println("User not found for email: " + email);
            return null;
        }
        
        System.out.println("User found: " + user.getEmail() + ", Company: " + (user.getCompany() != null ? user.getCompany().getName() : "null"));
        
        // Find profile by user ID
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        
        if (profile == null) {
            System.out.println("Profile not found for user: " + user.getEmail() + ", creating new profile");
            
            // Create empty profile for user
            profile = new Profile();
            profile.setUserId(user.getId());
            try {
                profile.setProfileData(objectMapper.readTree("{}"));
            } catch (Exception e) {
                System.out.println("Error creating empty profile data: " + e.getMessage());
            }
            profile.setStatus(ProfileStatus.PENDING);
            profile.setLastUpdated(OffsetDateTime.now());
            
            if (user.getCompany() != null) {
                profile.setCompanyId(user.getCompany().getId());
            }
            
            profile = profileRepository.save(profile);
            System.out.println("New profile created for user: " + user.getEmail());
        }
        
        System.out.println("Profile found for user: " + user.getEmail());
        
        // Return a simplified profile object for frontend
        final User finalUser = user;
        final Profile finalProfile = profile;
        return new Object() {
            public final UUID userId = finalProfile.getUserId();
            public final String employeeEmail = finalUser.getEmail();
            public final String employeeName = finalUser.getFullName();
            public final String profileData = finalProfile.getProfileData() != null ? finalProfile.getProfileData().toString() : "{}";
            public final ProfileStatus status = finalProfile.getStatus();
            public final boolean isVerified = finalProfile.getStatus() == ProfileStatus.COMPLETED;
            public final UUID companyId = finalUser.getCompany() != null ? finalUser.getCompany().getId() : null;
            public final String companyName = finalUser.getCompany() != null ? finalUser.getCompany().getName() : null;
        };
    }
    
    public Object createEmployeeProfile(CreateProfileRequest request) {
        System.out.println("Creating employee profile for email: " + request.getEmployeeEmail());
        System.out.println("Request data: " + request);
        
        // Get current company from security context (or use default if not authenticated)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User companyUser = null;
        
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String currentUserEmail = auth.getName();
            System.out.println("Current user email: " + currentUserEmail);
            
            companyUser = userRepository.findByEmail(currentUserEmail).orElse(null);
            
            if (companyUser != null) {
                System.out.println("Current user role: " + companyUser.getRole());
                System.out.println("Current user company: " + (companyUser.getCompany() != null ? companyUser.getCompany().getName() : "null"));
            }
        }
        
        // If no authenticated user or no company, create/use a default company
        if (companyUser == null || companyUser.getCompany() == null) {
            System.out.println("No authenticated user or company, creating default company");
            Company company = companyRepository.findByName("Default Company").orElse(null);
            
            if (company == null) {
                company = new Company();
                company.setName("Default Company");
                company = companyRepository.save(company);
                System.out.println("Created default company: " + company.getName());
            }
            
            // If we have a user, associate them with the company
            if (companyUser != null && companyUser.getCompany() == null) {
                companyUser.setCompany(company);
                companyUser = userRepository.save(companyUser);
            }
            
            companyUser = new User();
            companyUser.setCompany(company);
        }
        
        Company userCompany = companyUser.getCompany();
        System.out.println("Using company: " + userCompany.getName());
        
        // Check if user with this email already exists
        Optional<User> existingUser = userRepository.findByEmail(request.getEmployeeEmail());
        User user;
        
        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Update company association if needed
            if (user.getCompany() == null) {
                user.setCompany(userCompany);
                user = userRepository.save(user);
            }
        } else {
            // Create new user for the employee
            user = new User();
            user.setEmail(request.getEmployeeEmail());
            user.setFullName(request.getEmployeeName());
            user.setRole(org.example.new_new_mvp.model.UserRole.EMPLOYEE);
            user.setCompany(userCompany); // Associate with company
            user.setActivationCode(UUID.randomUUID().toString()); // Генерируем activation_code
            user.setCreatedAt(java.time.OffsetDateTime.now());
            user.setStatus("invited"); // Статус "приглашен"
            user = userRepository.save(user);
            
            System.out.println("Created new user: " + user.getEmail() + ", ID: " + user.getId() + ", activation_code: " + user.getActivationCode());
        }
        
        // Verify user has an ID
        if (user.getId() == null) {
            throw new RuntimeException("Ошибка: не удалось определить ID пользователя после сохранения");
        }
        
        System.out.println("Using user ID: " + user.getId() + " (type: " + user.getId().getClass() + ") for profile creation");
        
        // Create profile data JSON
        String profileDataJson = String.format("""
            {
                "currentPosition": "%s",
                "currentSkills": "%s", 
                "currentResponsibilities": "%s",
                "desiredPosition": "%s",
                "desiredSkills": "%s",
                "careerGoals": "%s"
            }
            """, 
            request.getCurrentPosition() != null ? request.getCurrentPosition().replace("\"", "\\\"") : "",
            request.getCurrentSkills() != null ? request.getCurrentSkills().replace("\"", "\\\"") : "",
            request.getCurrentResponsibilities() != null ? request.getCurrentResponsibilities().replace("\"", "\\\"") : "",
            request.getDesiredPosition() != null ? request.getDesiredPosition().replace("\"", "\\\"") : "",
            request.getDesiredSkills() != null ? request.getDesiredSkills().replace("\"", "\\\"") : "",
            request.getCareerGoals() != null ? request.getCareerGoals().replace("\"", "\\\"") : ""
        );
        
        // Create or update profile
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(new Profile());
        profile.setUserId(user.getId());
        try {
            profile.setProfileData(objectMapper.readTree(profileDataJson));
        } catch (Exception e) {
            throw new RuntimeException("Error parsing profile data", e);
        }
        profile.setCompanyId(userCompany.getId()); // Save company_id
        profile.setStatus(ProfileStatus.COMPLETED); // Set transient status
        profile.setLastUpdated(java.time.OffsetDateTime.now());
        
        System.out.println("Saving profile with user_id: " + profile.getUserId() + ", company_id: " + profile.getCompanyId());
        
        Profile savedProfile = profileRepository.save(profile);
        System.out.println("Profile saved with ID: " + savedProfile.getUserId());
        
        // Create snapshot - TEMPORARILY DISABLED
        // TODO: Implement proper snapshot creation with Supabase user lookup
        // createSnapshot(user.getId(), profileDataJson);
        
        // Return simplified result for frontend
        final User finalUser = user;
        return new Object() {
            public final String message = "Profile created successfully";
            public final UUID userId = savedProfile.getUserId();
            public final String employeeEmail = finalUser.getEmail();
            public final String employeeName = finalUser.getFullName();
            public final ProfileStatus status = savedProfile.getStatus();
        };
    }
    
    private ProfileDto convertToDto(Profile profile, User user) {
        ProfileDto dto = new ProfileDto();
        dto.setUserId(profile.getUserId());
        dto.setUserEmail(user.getEmail());
        dto.setUserFullName(user.getFullName());
        dto.setProfileData(profile.getProfileData());
        dto.setStatus(profile.getStatus());
        dto.setLastUpdated(profile.getLastUpdated());
        
        return dto;
    }
    
    private ProfileDto convertToDto(Profile profile) {
        // Get user information separately since we removed the direct relationship
        User user = userRepository.findById(profile.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found for profile"));
        return convertToDto(profile, user);
    }
    
    /**
     * Кнопка 3: Создать мой AI-профиль
     * Получает email из JWT токена, ищет пользователя по email в Supabase,
     * собирает все данные и отправляет на webhook
     */
    public Object generateAIProfile(UUID profileId) {
        System.out.println("Generating AI profile for profile ID: " + profileId);
        
        // Get current user email from authentication context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authentication object: " + auth);
        System.out.println("Is authenticated: " + (auth != null ? auth.isAuthenticated() : "null"));
        System.out.println("Principal: " + (auth != null ? auth.getName() : "null"));
        
        String userEmail = null;
        
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            userEmail = auth.getName();
            System.out.println("Current user email from token: " + userEmail);
        } else {
            // Fallback: try to find user by profileId (which is actually userId)
            System.out.println("No authentication found, trying to find user by profileId: " + profileId);
            User userById = userRepository.findById(profileId).orElse(null);
            if (userById != null) {
                userEmail = userById.getEmail();
                System.out.println("Found user by ID: " + userEmail);
            }
        }
        
        if (userEmail == null) {
            throw new RuntimeException("User not authenticated and could not be found by ID");
        }
        
        final String currentUserEmail = userEmail;
        
        // Find user by email to get ALL data including activation_code
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found by email: " + currentUserEmail));
        
        System.out.println("User found: " + user.getEmail() + ", activation_code: " + user.getActivationCode());
        
        // Try to find profile by user ID
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        
        if (profile == null) {
            System.out.println("Profile not found for user, creating empty profile");
            // Create empty profile
            profile = new Profile();
            profile.setUserId(user.getId());
            try {
                profile.setProfileData(objectMapper.readTree("{}"));
            } catch (Exception e) {
                System.out.println("Error creating empty profile data: " + e.getMessage());
            }
            profile.setStatus(ProfileStatus.PENDING);
            profile.setLastUpdated(OffsetDateTime.now());
            
            if (user.getCompany() != null) {
                profile.setCompanyId(user.getCompany().getId());
            }
            
            profile = profileRepository.save(profile);
            System.out.println("Empty profile created for user: " + user.getEmail());
        }
        
        final User finalUser = user;
        final Profile finalProfile = profile;
        
        System.out.println("Preparing to send webhook with activation_code: " + finalUser.getActivationCode());
        
        try {
            // Send webhook to n8n (третий webhook) со ВСЕМИ данными пользователя из Supabase
            String webhookResponse = webhookService.sendAIProfileGenerationWebhook(
                finalProfile.getUserId(),
                finalUser.getEmail(),
                finalUser.getFullName(),
                finalProfile.getProfileData() != null ? finalProfile.getProfileData().toString() : "{}",
                finalUser.getCompany() != null ? finalUser.getCompany().getName() : null,
                finalUser.getActivationCode(),
                finalUser.getTelegramChatId(),
                finalUser.getStatus()
            );
            
            System.out.println("Webhook sent successfully to n8n");
            
            // Return success response
            final String finalWebhookResponse = webhookResponse;
            return new Object() {
                public final String message = "AI profile generation initiated successfully";
                public final UUID userId = finalProfile.getUserId();
                public final String userEmail = finalUser.getEmail();
                public final String activationCode = finalUser.getActivationCode();
                public final String status = "webhook_sent";
                public final String webhookResponse = finalWebhookResponse;
            };
            
        } catch (Exception e) {
            System.out.println("Error sending webhook to n8n: " + e.getMessage());
            e.printStackTrace();
            
            // Return error response
            return new Object() {
                public final String message = "Error sending webhook to n8n";
                public final UUID userId = finalProfile.getUserId();
                public final String userEmail = finalUser.getEmail();
                public final String status = "webhook_error";
                public final String error = e.getMessage();
            };
        }
    }

    // Overload: allow passing activationCode/email explicitly (no auth required)
    public Object generateAIProfile(UUID profileId, String activationCode, String email) {
        System.out.println("Generating AI profile (explicit identifiers) for profile ID: " + profileId);
        System.out.println("Provided activationCode: " + activationCode + ", email: " + email);
        
        User user = null;
        
        if (activationCode != null && !activationCode.isBlank()) {
            user = userRepository.findByActivationCode(activationCode).orElse(null);
            if (user != null) {
                System.out.println("User found by activation_code: " + user.getEmail());
            }
        }
        
        if (user == null && email != null && !email.isBlank()) {
            user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                System.out.println("User found by email: " + user.getEmail());
            }
        }
        
        if (user == null) {
            // fallback to existing logic (auth or profileId path)
            return generateAIProfile(profileId);
        }
        
        // Ensure profile exists
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            System.out.println("Profile not found for user, creating empty profile");
            profile = new Profile();
            profile.setUserId(user.getId());
            try {
                profile.setProfileData(objectMapper.readTree("{}"));
            } catch (Exception ignored) {}
            profile.setStatus(ProfileStatus.PENDING);
            profile.setLastUpdated(OffsetDateTime.now());
            if (user.getCompany() != null) profile.setCompanyId(user.getCompany().getId());
            profile = profileRepository.save(profile);
        }
        
        // Send webhook with full data (including activation code)
        String webhookResponse = webhookService.sendAIProfileGenerationWebhook(
            profile.getUserId(),
            user.getEmail(),
            user.getFullName(),
            profile.getProfileData() != null ? profile.getProfileData().toString() : "{}",
            user.getCompany() != null ? user.getCompany().getName() : null,
            user.getActivationCode(),
            user.getTelegramChatId(),
            user.getStatus()
        );
        final String finalWebhookResponse = webhookResponse;
        final User finalUser = user;
        final Profile finalProfile = profile;
        return new Object() {
            public final String message = "AI profile generation initiated successfully";
            public final UUID userId = finalProfile.getUserId();
            public final String userEmail = finalUser.getEmail();
            public final String activationCode = finalUser.getActivationCode();
            public final String status = "webhook_sent";
            public final String webhookResponse = finalWebhookResponse;
        };
    }
    
    /**
     * Назначить роль пользователю из таблицы job_roles
     * Кнопка 2: Выбрать роль из списка
     * Сохраняет jobRoleId в поле "Role" таблицы users в Supabase
     */
    public Object assignJobRoleToUser(UUID userId, UUID jobRoleId) {
        System.out.println("Assigning job role " + jobRoleId + " to user " + userId);
        
        try {
            // Get user
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Get job role
            JobRole jobRole = jobRoleRepository.findById(jobRoleId)
                .orElseThrow(() -> new RuntimeException("Job role not found"));
            
            // Обновляем users."Role" напрямую в Supabase
            boolean roleUpdated = false;
            if (user.getActivationCode() != null && !user.getActivationCode().isEmpty()) {
                roleUpdated = userRepository.updateUserRoleByActivationCode(user.getActivationCode(), jobRoleId);
            }
            if (!roleUpdated) {
                roleUpdated = userRepository.updateUserRoleByEmail(user.getEmail(), jobRoleId);
            }
            System.out.println("users.\"Role\" updated: " + roleUpdated);
            
            // Update user profile with job role information (temporary)
            Profile profile = profileRepository.findByUserId(userId).orElse(new Profile());
            
            String updatedProfileData = String.format(
                "{\"currentPosition\": \"%s\", \"jobRoleData\": %s, \"assignedRoleId\": \"%s\", \"description\": \"%s\"}",
                jobRole.getTitle(),
                jobRole.getRequirements() != null ? jobRole.getRequirements().toString() : "{}",
                jobRoleId,
                jobRole.getDescription() != null ? jobRole.getDescription().replace("\"", "\\\"") : ""
            );
            
            profile.setUserId(userId);
            try {
                profile.setProfileData(objectMapper.readTree(updatedProfileData));
            } catch (Exception e) {
                throw new RuntimeException("Error parsing profile data", e);
            }
            profile.setStatus(ProfileStatus.COMPLETED);
            profile.setLastUpdated(OffsetDateTime.now());
            
            Profile savedProfile = profileRepository.save(profile);
            
            System.out.println("Job role assigned to profile successfully");
            
            // Отправляем webhook на второй n8n endpoint
            try {
                String webhookResponse = webhookService.sendJobRoleAssignmentWebhook(
                    savedProfile.getUserId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getActivationCode(),
                    jobRoleId,
                    jobRole.getTitle(),
                    jobRole.getDescription(),
                    savedProfile.getProfileData() != null ? savedProfile.getProfileData().toString() : "{}",
                    user.getCompany() != null ? user.getCompany().getName() : null
                );
                System.out.println("Webhook sent successfully: " + webhookResponse);
            } catch (Exception webhookException) {
                System.out.println("Error sending webhook (non-critical): " + webhookException.getMessage());
            }
            
            final UUID finalJobRoleId = jobRoleId;
            return new Object() {
                public final String message = "Job role assigned successfully";
                public final UUID userId = user.getId();
                public final String userEmail = user.getEmail();
                public final String jobRoleTitle = jobRole.getTitle();
                public final UUID jobRoleId = finalJobRoleId;
                public final String status = "success";
            };
            
        } catch (Exception e) {
            System.out.println("Error assigning job role: " + e.getMessage());
            e.printStackTrace();
            
            return new Object() {
                public final String message = "Error assigning job role";
                public final String error = e.getMessage();
                public final String status = "error";
            };
        }
    }

    // Новый способ: назначить роль по activationCode/email без userId
    public Object assignJobRoleFlexible(UUID jobRoleId, String activationCode, String email) {
        System.out.println("Assigning job role (flex) jobRoleId=" + jobRoleId + ", activationCode=" + activationCode + ", email=" + email);
        User user = null;
        if (activationCode != null && !activationCode.isBlank()) {
            user = userRepository.findByActivationCode(activationCode).orElse(null);
        }
        if (user == null && email != null && !email.isBlank()) {
            user = userRepository.findByEmail(email).orElse(null);
        }
        if (user == null) {
            throw new RuntimeException("User not found for provided identifiers");
        }
        // Выполняем назначение роли, минуя поиск по UUID
        try {
            JobRole jobRole = jobRoleRepository.findById(jobRoleId)
                .orElseThrow(() -> new RuntimeException("Job role not found"));

            // Обновляем users."Role"
            boolean roleUpdated = false;
            if (user.getActivationCode() != null && !user.getActivationCode().isEmpty()) {
                roleUpdated = userRepository.updateUserRoleByActivationCode(user.getActivationCode(), jobRoleId);
            }
            if (!roleUpdated) {
                roleUpdated = userRepository.updateUserRoleByEmail(user.getEmail(), jobRoleId);
            }
            System.out.println("users.\"Role\" updated (flex): " + roleUpdated);

            // Обновляем профиль (для UI и вебхука)
            Profile profile = profileRepository.findByUserId(user.getId()).orElse(new Profile());
            String updatedProfileData = String.format(
                "{\"currentPosition\": \"%s\", \"jobRoleData\": %s, \"assignedRoleId\": \"%s\", \"description\": \"%s\"}",
                jobRole.getTitle(),
                jobRole.getRequirements() != null ? jobRole.getRequirements().toString() : "{}",
                jobRoleId,
                jobRole.getDescription() != null ? jobRole.getDescription().replace("\"", "\\\"") : ""
            );
            profile.setUserId(user.getId());
            try {
                profile.setProfileData(objectMapper.readTree(updatedProfileData));
            } catch (Exception e) {
                throw new RuntimeException("Error parsing profile data", e);
            }
            profile.setStatus(ProfileStatus.COMPLETED);
            profile.setLastUpdated(OffsetDateTime.now());
            Profile savedProfile = profileRepository.save(profile);

            // Отправляем webhook с activationCode
            try {
                String webhookResponse = webhookService.sendJobRoleAssignmentWebhook(
                    savedProfile.getUserId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getActivationCode(),
                    jobRoleId,
                    jobRole.getTitle(),
                    jobRole.getDescription(),
                    savedProfile.getProfileData() != null ? savedProfile.getProfileData().toString() : "{}",
                    user.getCompany() != null ? user.getCompany().getName() : null
                );
                System.out.println("Webhook (flex) sent successfully: " + webhookResponse);
            } catch (Exception webhookException) {
                System.out.println("Error sending webhook (flex, non-critical): " + webhookException.getMessage());
            }

            final UUID finalJobRoleId = jobRoleId;
            final UUID finalUserId = user.getId();
            final String finalUserEmail = user.getEmail();
            final String finalJobRoleTitle = jobRole.getTitle();
            final boolean finalRoleUpdated = roleUpdated;
            return new Object() {
                public final String message = "Job role assigned successfully";
                public final UUID userId = finalUserId;
                public final String userEmail = finalUserEmail;
                public final String jobRoleTitle = finalJobRoleTitle;
                public final UUID jobRoleId = finalJobRoleId;
                public final String status = finalRoleUpdated ? "success" : "saved_profile_only";
            };

        } catch (Exception e) {
            System.out.println("Error assigning job role (flex): " + e.getMessage());
            e.printStackTrace();
            return new Object() {
                public final String message = "Error assigning job role";
                public final String error = e.getMessage();
                public final String status = "error";
            };
        }
    }
}
