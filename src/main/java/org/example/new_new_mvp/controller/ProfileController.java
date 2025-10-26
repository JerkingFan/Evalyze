package org.example.new_new_mvp.controller;

import org.example.new_new_mvp.dto.ProfileDto;
import org.example.new_new_mvp.dto.CreateProfileRequest;
import org.example.new_new_mvp.model.ProfileStatus;
import org.example.new_new_mvp.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = "*")
public class ProfileController {
    
    @Autowired
    private ProfileService profileService;
    
    @PostMapping("/user/{userId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<ProfileDto> createOrUpdateProfile(
            @PathVariable UUID userId,
            @RequestBody String profileData) {
        try {
            ProfileDto profile = profileService.createOrUpdateProfile(userId, profileData);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfileDto> getProfileByUserId(@PathVariable UUID userId) {
        return profileService.getProfileByUserId(userId)
                .map(profile -> ResponseEntity.ok(profile))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProfileDto>> getCompanyProfiles(@PathVariable UUID companyId) {
        List<ProfileDto> profiles = profileService.getCompanyProfiles(companyId);
        return ResponseEntity.ok(profiles);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProfileDto>> getProfilesByStatus(@PathVariable ProfileStatus status) {
        List<ProfileDto> profiles = profileService.getProfilesByStatus(status);
        return ResponseEntity.ok(profiles);
    }
    
    @PutMapping("/user/{userId}/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfileDto> updateProfileStatus(
            @PathVariable UUID userId,
            @PathVariable ProfileStatus status) {
        try {
            ProfileDto profile = profileService.updateProfileStatus(userId, status);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/user/{userId}/snapshots")
    public ResponseEntity<List<?>> getUserSnapshots(@PathVariable UUID userId) {
        List<?> snapshots = profileService.getUserSnapshots(userId);
        return ResponseEntity.ok(snapshots);
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchProfileByEmail(@RequestParam String email) {
        try {
            System.out.println("Search request for email: " + email);
            var profile = profileService.findProfileByEmail(email);
            if (profile != null) {
                System.out.println("Profile found, returning to client");
                return ResponseEntity.ok().body(profile);
            } else {
                System.out.println("Profile not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("Error searching profile: " + e.getMessage());
            e.printStackTrace();
            
            // Return proper JSON error response
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Error searching profile");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "error");
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/create")
    // @PreAuthorize("hasRole('COMPANY')") // Temporarily disabled for debugging
    public ResponseEntity<?> createEmployeeProfile(@RequestBody CreateProfileRequest request) {
        try {
            System.out.println("Creating profile request received for: " + request.getEmployeeEmail());
            var result = profileService.createEmployeeProfile(request);
            System.out.println("Profile creation successful");
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            System.out.println("Error creating profile: " + e.getMessage());
            e.printStackTrace();
            
            // Return proper JSON error response
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Error creating profile");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "error");
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<?>> getAllProfiles() {
        try {
            System.out.println("Getting all profiles");
            List<?> profiles = profileService.getAllProfiles();
            System.out.println("Found " + profiles.size() + " profiles");
            return ResponseEntity.ok(profiles);
        } catch (Exception e) {
            System.out.println("Error getting all profiles: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/generate-ai/{profileId}")
    public ResponseEntity<?> generateAIProfile(@PathVariable UUID profileId, @RequestBody(required = false) java.util.Map<String, String> body) {
        try {
            System.out.println("Generating AI profile for ID: " + profileId);
            String activationCode = body != null ? body.get("activationCode") : null;
            String email = body != null ? body.get("email") : null;
            var result = profileService.generateAIProfile(profileId, activationCode, email);
            System.out.println("AI profile generation initiated successfully");
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            System.out.println("Error generating AI profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error generating AI profile: " + e.getMessage());
        }
    }
    
    @PostMapping("/assign-role/{userId}")
    public ResponseEntity<?> assignJobRole(@PathVariable UUID userId, @RequestBody String payload) {
        try {
            System.out.println("Assigning job role to user " + userId + "; payload=" + payload);
            java.util.UUID jobRoleId = null;
            String email = null;
            String activationCode = null;
            
            try {
                if (payload != null && payload.trim().startsWith("{")) {
                    var node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(payload);
                    if (node.has("jobRoleId")) jobRoleId = java.util.UUID.fromString(node.get("jobRoleId").asText());
                    if (node.has("email")) email = node.get("email").asText(null);
                    if (node.has("activationCode")) activationCode = node.get("activationCode").asText(null);
                } else {
                    // raw UUID string
                    String raw = payload == null ? "" : payload.replace("\"", "").trim();
                    if (!raw.isEmpty()) jobRoleId = java.util.UUID.fromString(raw);
                }
            } catch (Exception parseEx) {
                System.out.println("Failed to parse payload, trying as raw UUID: " + parseEx.getMessage());
                String raw = payload == null ? "" : payload.replace("\"", "").trim();
                if (!raw.isEmpty()) jobRoleId = java.util.UUID.fromString(raw);
            }
            
            if (jobRoleId == null) {
                throw new RuntimeException("jobRoleId is required");
            }
            
            Object result;
            if ((email != null && !email.isBlank()) || (activationCode != null && !activationCode.isBlank())) {
                System.out.println("Using flexible assignment with identifiers: email=" + email + ", activationCode=" + activationCode);
                result = profileService.assignJobRoleFlexible(jobRoleId, activationCode, email);
            } else {
                result = profileService.assignJobRoleToUser(userId, jobRoleId);
            }
            
            System.out.println("Job role assigned successfully");
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            System.out.println("Error assigning job role: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error assigning job role: " + e.getMessage());
        }
    }

    @PostMapping("/assign-role")
    public ResponseEntity<?> assignJobRoleFlexible(@RequestBody java.util.Map<String, String> body) {
        try {
            String jobRoleIdStr = body.get("jobRoleId");
            String activationCode = body.get("activationCode");
            String email = body.get("email");
            System.out.println("Assigning job role (flex) jobRoleId=" + jobRoleIdStr + ", email=" + email + ", activationCode=" + activationCode);
            java.util.UUID jobRoleId = java.util.UUID.fromString(jobRoleIdStr);
            var result = profileService.assignJobRoleFlexible(jobRoleId, activationCode, email);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("Error assigning job role (flex): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error assigning job role: " + e.getMessage());
        }
    }
}
