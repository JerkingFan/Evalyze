package org.example.new_new_mvp.controller;

import org.example.new_new_mvp.model.Company;
import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.model.UserRole;
import org.example.new_new_mvp.repository.JpaCompanyRepository;
import org.example.new_new_mvp.repository.JpaUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/jpa-test")
public class JpaTestController {

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private JpaCompanyRepository companyRepository;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/companies")
    public ResponseEntity<List<Company>> getAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        return ResponseEntity.ok(companies);
    }

    @PostMapping("/company")
    public ResponseEntity<Company> createCompany(@RequestBody Map<String, String> request) {
        Company company = new Company();
        company.setName(request.get("name"));
        company.setCreatedAt(java.time.OffsetDateTime.now());
        
        Company savedCompany = companyRepository.save(company);
        return ResponseEntity.ok(savedCompany);
    }

    @PostMapping("/user")
    public ResponseEntity<User> createUser(@RequestBody Map<String, String> request) {
        User user = new User();
        user.setEmail(request.get("email"));
        user.setFullName(request.get("fullName"));
        user.setRole(UserRole.valueOf(request.get("role")));
        
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testJpaConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long userCount = userRepository.count();
            long companyCount = companyRepository.count();
            
            response.put("status", "success");
            response.put("message", "JPA connection to Supabase PostgreSQL successful!");
            response.put("userCount", userCount);
            response.put("companyCount", companyCount);
            response.put("timestamp", java.time.LocalDateTime.now());
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "JPA connection failed: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
        }
        
        return ResponseEntity.ok(response);
    }
}
