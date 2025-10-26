package org.example.new_new_mvp.controller;

import org.example.new_new_mvp.dto.JobRoleDto;
import org.example.new_new_mvp.model.JobRole;
import org.example.new_new_mvp.repository.JobRoleRepositoryAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/job-roles")
@CrossOrigin(origins = "*")
public class JobRoleController {
    
    private final JobRoleRepositoryAdapter jobRoleRepository;
    
    @Autowired
    public JobRoleController(JobRoleRepositoryAdapter jobRoleRepository) {
        this.jobRoleRepository = jobRoleRepository;
    }
    
    @GetMapping
    public ResponseEntity<List<JobRoleDto>> getAllJobRoles() {
        try {
            System.out.println("Getting all job roles...");
            List<JobRole> jobRoles = jobRoleRepository.findAll();
            System.out.println("Found " + jobRoles.size() + " job roles");
            
            List<JobRoleDto> dtos = jobRoles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            
            System.out.println("Converted to " + dtos.size() + " DTOs");
            return ResponseEntity.ok(dtos);
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException.Forbidden e) {
            System.err.println("403 Forbidden: " + e.getMessage());
            System.err.println("Response body: " + e.getResponseBodyAsString());
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            System.err.println("Error getting job roles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/by-company/{companyId}")
    public ResponseEntity<List<JobRoleDto>> getJobRolesByCompany(@PathVariable UUID companyId) {
        List<JobRole> jobRoles = jobRoleRepository.findByCompanyId(companyId);
        List<JobRoleDto> dtos = jobRoles.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/by-type/{roleType}")
    public ResponseEntity<List<JobRoleDto>> getJobRolesByType(@PathVariable String roleType) {
        List<JobRole> jobRoles = jobRoleRepository.findByRoleType(roleType);
        List<JobRoleDto> dtos = jobRoles.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<JobRoleDto> getJobRoleById(@PathVariable UUID id) {
        return jobRoleRepository.findById(id)
            .map(this::convertToDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    private JobRoleDto convertToDto(JobRole role) {
        JobRoleDto dto = new JobRoleDto();
        dto.setId(role.getId());
        dto.setCompanyId(role.getCompanyId());
        dto.setRoleType(role.getRoleType());
        dto.setTitle(role.getTitle());
        dto.setDescription(role.getDescription());
        dto.setRequirements(role.getRequirements() != null ? role.getRequirements().toString() : null);
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        return dto;
    }
}
