package org.example.new_new_mvp.service;

import org.example.new_new_mvp.dto.ProfileDto;
import org.example.new_new_mvp.dto.CreateProfileRequest;
import org.example.new_new_mvp.model.Profile;
import org.example.new_new_mvp.model.ProfileSnapshot;
import org.example.new_new_mvp.model.ProfileStatus;
import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.model.UserRole;
import org.example.new_new_mvp.model.Company;
import org.example.new_new_mvp.repository.ProfileRepository;
import org.example.new_new_mvp.repository.ProfileSnapshotRepository;
import org.example.new_new_mvp.repository.UserRepository;
import org.example.new_new_mvp.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private WebhookService webhookService;
    
    public ProfileDto createOrUpdateProfile(UUID userId, String profileData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Profile profile = profileRepository.findByUserId(userId).orElse(new Profile());
        profile.setUserId(userId);
        profile.setProfileData(profileData);
        profile.setStatus(ProfileStatus.PENDING);
        profile.setLastUpdated(java.time.LocalDateTime.now());
        
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
        return profileRepository.findByStatus(status).stream()
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
        snapshot.setSnapshotDate(java.time.LocalDateTime.now());
        
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
            public final String profileData = profile.getProfileData();
            public final ProfileStatus status = profile.getStatus();
            public final boolean isVerified = profile.getStatus() == ProfileStatus.COMPLETED;
            public final boolean aiProfileGenerated = false; // For now, always false
            public final LocalDateTime lastUpdated = profile.getLastUpdated();
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
            System.out.println("Profile not found for user: " + user.getEmail());
            return null;
        }
        
        System.out.println("Profile found for user: " + user.getEmail());
        
        // Return a simplified profile object for frontend
        final User finalUser = user;
        return new Object() {
            public final UUID userId = profile.getUserId();
            public final String employeeEmail = finalUser.getEmail();
            public final String employeeName = finalUser.getFullName();
            public final String profileData = profile.getProfileData();
            public final ProfileStatus status = profile.getStatus();
            public final boolean isVerified = profile.getStatus() == ProfileStatus.COMPLETED;
            public final UUID companyId = finalUser.getCompany() != null ? finalUser.getCompany().getId() : null;
            public final String companyName = finalUser.getCompany() != null ? finalUser.getCompany().getName() : null;
        };
    }
    
    public Object createEmployeeProfile(CreateProfileRequest request) {
        System.out.println("Creating employee profile for email: " + request.getEmployeeEmail());
        
        // Get current company from security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName(); // email of the company user
        System.out.println("Current user email: " + currentUserEmail);
        
        User companyUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        
        System.out.println("Current user role: " + companyUser.getRole());
        System.out.println("Current user company: " + (companyUser.getCompany() != null ? companyUser.getCompany().getName() : "null"));
        
        if (companyUser.getCompany() == null) {
            // Try to create a company for this user if they have COMPANY role
            if (companyUser.getRole() == UserRole.COMPANY) {
                System.out.println("Creating company for user: " + currentUserEmail);
                Company company = new Company();
                company.setName(companyUser.getFullName() + " Company");
                company = companyRepository.save(company);
                companyUser.setCompany(company);
                companyUser = userRepository.save(companyUser);
                System.out.println("Created company: " + company.getName());
            } else {
                throw new RuntimeException("Current user is not associated with a company and is not a COMPANY user");
            }
        }
        
        System.out.println("Company: " + companyUser.getCompany().getName());
        
        // Check if user with this email already exists
        Optional<User> existingUser = userRepository.findByEmail(request.getEmployeeEmail());
        User user;
        
        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Update company association if needed
            if (user.getCompany() == null) {
                user.setCompany(companyUser.getCompany());
                user = userRepository.save(user);
            }
        } else {
            // Create new user for the employee
            user = new User();
            user.setEmail(request.getEmployeeEmail());
            user.setFullName(request.getEmployeeName());
            user.setRole(org.example.new_new_mvp.model.UserRole.EMPLOYEE);
            user.setCompany(companyUser.getCompany()); // Associate with current company
            user = userRepository.save(user);
        }
        
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
        profile.setProfileData(profileDataJson);
        profile.setStatus(ProfileStatus.COMPLETED); // Сразу активный профиль
        profile.setLastUpdated(java.time.LocalDateTime.now());
        
        Profile savedProfile = profileRepository.save(profile);
        System.out.println("Profile saved with ID: " + savedProfile.getUserId());
        
        // Create snapshot
        createSnapshot(user.getId(), profileDataJson);
        
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
    
    public Object generateAIProfile(UUID profileId) {
        System.out.println("Generating AI profile for profile ID: " + profileId);
        
        // Find profile by user ID (since profileId is actually userId)
        Profile profile = profileRepository.findByUserId(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        
        // Get user information separately
        User user = userRepository.findById(profile.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        System.out.println("Profile found for user: " + user.getEmail());
        
        try {
            // Send webhook to n8n
            String webhookResponse = webhookService.sendProfileWebhook(
                profile.getUserId(),
                user.getEmail(),
                user.getFullName(),
                profile.getProfileData(),
                user.getCompany() != null ? user.getCompany().getName() : null
            );
            
            // Return success response
            final String finalWebhookResponse = webhookResponse;
            return new Object() {
                public final String message = "AI profile generation initiated successfully";
                public final UUID userId = profile.getUserId();
                public final String userEmail = user.getEmail();
                public final String status = "webhook_sent";
                public final String webhookResponse = finalWebhookResponse;
            };
            
        } catch (Exception e) {
            System.out.println("Error sending webhook to n8n: " + e.getMessage());
            e.printStackTrace();
            
            // Return error response
            final User finalUser = user;
            return new Object() {
                public final String message = "Error sending webhook to n8n";
                public final UUID userId = profile.getUserId();
                public final String userEmail = finalUser.getEmail();
                public final String status = "webhook_error";
                public final String error = e.getMessage();
            };
        }
    }
}
