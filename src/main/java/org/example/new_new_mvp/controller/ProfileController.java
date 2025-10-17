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
            return ResponseEntity.badRequest().body("Error searching profile: " + e.getMessage());
        }
    }
    
    @PostMapping("/create")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<?> createEmployeeProfile(@RequestBody CreateProfileRequest request) {
        try {
            System.out.println("Creating profile request received for: " + request.getEmployeeEmail());
            var result = profileService.createEmployeeProfile(request);
            System.out.println("Profile creation successful");
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            System.out.println("Error creating profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating profile: " + e.getMessage());
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
    public ResponseEntity<?> generateAIProfile(@PathVariable UUID profileId) {
        try {
            System.out.println("Generating AI profile for ID: " + profileId);
            var result = profileService.generateAIProfile(profileId);
            System.out.println("AI profile generation initiated successfully");
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            System.out.println("Error generating AI profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error generating AI profile: " + e.getMessage());
        }
    }
}
