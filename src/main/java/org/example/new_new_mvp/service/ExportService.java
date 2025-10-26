package org.example.new_new_mvp.service;

import org.example.new_new_mvp.model.*;
import org.example.new_new_mvp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExportService {
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProfileRepository profileRepository;
    
    @Autowired
    private ProfileSnapshotRepository profileSnapshotRepository;
    
    @Autowired
    private InvitationRepository invitationRepository;
    
    @Autowired
    private CompanyContentRepository companyContentRepository;
    
    public Map<String, Object> exportAllData() {
        System.out.println("Starting full database export...");
        
        Map<String, Object> exportData = new HashMap<>();
        
        // Export metadata
        exportData.put("exportInfo", createExportInfo());
        
        // Export all tables
        exportData.put("companies", exportCompanies());
        exportData.put("users", exportUsers());
        exportData.put("profiles", exportProfiles());
        exportData.put("profileSnapshots", exportProfileSnapshots());
        exportData.put("invitations", exportInvitations());
        exportData.put("companyContent", exportCompanyContent());
        
        // Export statistics
        exportData.put("statistics", createStatistics());
        
        System.out.println("Database export completed successfully");
        return exportData;
    }
    
    public Map<String, Object> exportCompanies() {
        List<Company> companies = companyRepository.findAll();
        
        Map<String, Object> result = new HashMap<>();
        result.put("count", companies.size());
        result.put("data", companies.stream().map(this::convertCompanyToMap).collect(Collectors.toList()));
        
        return result;
    }
    
    public Map<String, Object> exportUsers() {
        List<User> users = userRepository.findAll();
        
        Map<String, Object> result = new HashMap<>();
        result.put("count", users.size());
        result.put("data", users.stream().map(this::convertUserToMap).collect(Collectors.toList()));
        
        return result;
    }
    
    public Map<String, Object> exportProfiles() {
        List<Profile> profiles = profileRepository.findAll();
        
        Map<String, Object> result = new HashMap<>();
        result.put("count", profiles.size());
        result.put("data", profiles.stream().map(this::convertProfileToMap).collect(Collectors.toList()));
        
        return result;
    }
    
    public Map<String, Object> exportProfileSnapshots() {
        List<ProfileSnapshot> snapshots = profileSnapshotRepository.findAll();
        
        Map<String, Object> result = new HashMap<>();
        result.put("count", snapshots.size());
        result.put("data", snapshots.stream().map(this::convertProfileSnapshotToMap).collect(Collectors.toList()));
        
        return result;
    }
    
    public Map<String, Object> exportInvitations() {
        List<Invitation> invitations = invitationRepository.findAll();
        
        Map<String, Object> result = new HashMap<>();
        result.put("count", invitations.size());
        result.put("data", invitations.stream().map(this::convertInvitationToMap).collect(Collectors.toList()));
        
        return result;
    }
    
    public Map<String, Object> exportCompanyContent() {
        List<CompanyContent> content = companyContentRepository.findAll();
        
        Map<String, Object> result = new HashMap<>();
        result.put("count", content.size());
        result.put("data", content.stream().map(this::convertCompanyContentToMap).collect(Collectors.toList()));
        
        return result;
    }
    
    private Map<String, Object> createExportInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("exportDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        info.put("version", "1.0");
        info.put("description", "Evalyze Database Export");
        return info;
    }
    
    private Map<String, Object> createStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalCompanies", companyRepository.count());
        stats.put("totalUsers", userRepository.count());
        stats.put("totalProfiles", profileRepository.count());
        stats.put("totalSnapshots", profileSnapshotRepository.count());
        stats.put("totalInvitations", invitationRepository.count());
        stats.put("totalContent", companyContentRepository.count());
        
        // Additional statistics
        stats.put("usersByRole", getUserCountByRole());
        stats.put("profilesByStatus", getProfileCountByStatus());
        stats.put("invitationsByStatus", getInvitationCountByStatus());
        
        return stats;
    }
    
    private Map<String, Long> getUserCountByRole() {
        Map<String, Long> roleCounts = new HashMap<>();
        for (UserRole role : UserRole.values()) {
            long count = userRepository.countByRole(role);
            roleCounts.put(role.name(), count);
        }
        return roleCounts;
    }
    
    private Map<String, Long> getProfileCountByStatus() {
        Map<String, Long> statusCounts = new HashMap<>();
        // Status field doesn't exist in Supabase schema - return 0 for all statuses
        for (ProfileStatus status : ProfileStatus.values()) {
            statusCounts.put(status.name(), 0L);
        }
        return statusCounts;
    }
    
    private Map<String, Long> getInvitationCountByStatus() {
        Map<String, Long> statusCounts = new HashMap<>();
        for (InvitationStatus status : InvitationStatus.values()) {
            long count = invitationRepository.countByStatus(status);
            statusCounts.put(status.name(), count);
        }
        return statusCounts;
    }
    
    // Conversion methods
    private Map<String, Object> convertCompanyToMap(Company company) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", company.getId());
        map.put("name", company.getName());
        map.put("createdAt", company.getCreatedAt());
        return map;
    }
    
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("email", user.getEmail());
        map.put("fullName", user.getFullName());
        map.put("role", user.getRole());
        map.put("companyId", user.getCompany() != null ? user.getCompany().getId() : null);
        map.put("companyName", user.getCompany() != null ? user.getCompany().getName() : null);
        // Google OAuth token removed - using email auth instead
        return map;
    }
    
    private Map<String, Object> convertProfileToMap(Profile profile) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", profile.getUserId());
        
        // Get user information separately since we removed the direct relationship
        User user = userRepository.findById(profile.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found for profile"));
        
        map.put("userEmail", user.getEmail());
        map.put("userFullName", user.getFullName());
        map.put("profileData", profile.getProfileData());
        map.put("status", profile.getStatus());
        map.put("lastUpdated", profile.getLastUpdated());
        return map;
    }
    
    private Map<String, Object> convertProfileSnapshotToMap(ProfileSnapshot snapshot) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", snapshot.getId());
        
        // Get user information separately since we removed the direct relationship
        User user = userRepository.findById(snapshot.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found for snapshot"));
        
        map.put("userId", user.getId());
        map.put("userEmail", user.getEmail());
        map.put("snapshotDate", snapshot.getSnapshotDate());
        map.put("profileData", snapshot.getProfileData());
        return map;
    }
    
    private Map<String, Object> convertInvitationToMap(Invitation invitation) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", invitation.getId());
        map.put("companyId", invitation.getCompany().getId());
        map.put("companyName", invitation.getCompany().getName());
        map.put("email", invitation.getEmail());
        map.put("invitationCode", invitation.getInvitationCode());
        map.put("status", invitation.getStatus());
        map.put("expiresAt", invitation.getExpiresAt());
        map.put("expired", invitation.isExpired());
        return map;
    }
    
    private Map<String, Object> convertCompanyContentToMap(CompanyContent content) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", content.getId());
        map.put("companyId", content.getCompany().getId());
        map.put("companyName", content.getCompany().getName());
        map.put("contentType", content.getContentType());
        map.put("title", content.getTitle());
        map.put("data", content.getData());
        return map;
    }
    
    public String exportSQL() {
        System.out.println("Generating SQL export...");
        
        StringBuilder sql = new StringBuilder();
        
        // Header
        sql.append("-- Evalyze Database SQL Export\n");
        sql.append("-- Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n");
        sql.append("-- Description: Complete database export with INSERT statements\n\n");
        
        // Export companies
        sql.append("-- Companies\n");
        sql.append("DELETE FROM companies CASCADE;\n");
        List<Company> companies = companyRepository.findAll();
        for (Company company : companies) {
            sql.append(String.format("INSERT INTO companies (id, name, created_at) VALUES ('%s', '%s', '%s');\n",
                company.getId(),
                escapeSql(company.getName()),
                company.getCreatedAt()
            ));
        }
        sql.append("\n");
        
        // Export users
        sql.append("-- Users\n");
        sql.append("DELETE FROM users CASCADE;\n");
        List<User> users = userRepository.findAll();
        for (User user : users) {
            sql.append(String.format("INSERT INTO users (id, company_id, email, full_name, role, google_oauth_token) VALUES ('%s', %s, '%s', '%s', '%s', %s);\n",
                user.getId(),
                user.getCompany() != null ? "'" + user.getCompany().getId() + "'" : "NULL",
                escapeSql(user.getEmail()),
                escapeSql(user.getFullName()),
                user.getRole(),
                "NULL" // Google OAuth token removed - using email auth instead
            ));
        }
        sql.append("\n");
        
        // Export profiles
        sql.append("-- Profiles\n");
        sql.append("DELETE FROM profiles CASCADE;\n");
        List<Profile> profiles = profileRepository.findAll();
        for (Profile profile : profiles) {
            sql.append(String.format("INSERT INTO profiles (user_id, profile_data, status, last_updated) VALUES ('%s', '%s', '%s', '%s');\n",
                profile.getUserId(),
                escapeSql(profile.getProfileData() != null ? profile.getProfileData().toString() : ""),
                profile.getStatus(),
                profile.getLastUpdated()
            ));
        }
        sql.append("\n");
        
        // Export profile snapshots
        sql.append("-- Profile Snapshots\n");
        sql.append("DELETE FROM profile_snapshots CASCADE;\n");
        List<ProfileSnapshot> snapshots = profileSnapshotRepository.findAll();
        for (ProfileSnapshot snapshot : snapshots) {
            sql.append(String.format("INSERT INTO profile_snapshots (id, user_id, snapshot_date, profile_data) VALUES ('%s', '%s', '%s', '%s');\n",
                snapshot.getId(),
                snapshot.getUserId(),
                snapshot.getSnapshotDate(),
                escapeSql(snapshot.getProfileData() != null ? snapshot.getProfileData().toString() : "")
            ));
        }
        sql.append("\n");
        
        // Export invitations
        sql.append("-- Invitations\n");
        sql.append("DELETE FROM invitations CASCADE;\n");
        List<Invitation> invitations = invitationRepository.findAll();
        for (Invitation invitation : invitations) {
            sql.append(String.format("INSERT INTO invitations (id, company_id, email, invitation_code, status, expires_at) VALUES ('%s', '%s', '%s', '%s', '%s', %s);\n",
                invitation.getId(),
                invitation.getCompany().getId(),
                escapeSql(invitation.getEmail()),
                escapeSql(invitation.getInvitationCode()),
                invitation.getStatus(),
                invitation.getExpiresAt() != null ? "'" + invitation.getExpiresAt() + "'" : "NULL"
            ));
        }
        sql.append("\n");
        
        // Export company content
        sql.append("-- Company Content\n");
        sql.append("DELETE FROM company_content CASCADE;\n");
        List<CompanyContent> content = companyContentRepository.findAll();
        for (CompanyContent item : content) {
            sql.append(String.format("INSERT INTO company_content (id, company_id, content_type, title, data) VALUES ('%s', '%s', '%s', '%s', '%s');\n",
                item.getId(),
                item.getCompany().getId(),
                item.getContentType(),
                escapeSql(item.getTitle()),
                escapeSql(item.getData() != null ? item.getData().toString() : "")
            ));
        }
        sql.append("\n");
        
        // Statistics
        sql.append("-- Statistics\n");
        sql.append(String.format("-- Total Companies: %d\n", companyRepository.count()));
        sql.append(String.format("-- Total Users: %d\n", userRepository.count()));
        sql.append(String.format("-- Total Profiles: %d\n", profileRepository.count()));
        sql.append(String.format("-- Total Snapshots: %d\n", profileSnapshotRepository.count()));
        sql.append(String.format("-- Total Invitations: %d\n", invitationRepository.count()));
        sql.append(String.format("-- Total Content: %d\n", companyContentRepository.count()));
        
        System.out.println("SQL export completed successfully");
        return sql.toString();
    }
    
    private String escapeSql(String value) {
        if (value == null) return "";
        return value.replace("'", "''").replace("\n", "\\n").replace("\r", "\\r");
    }
}
