package org.example.new_new_mvp.service;

import org.example.new_new_mvp.dto.AuthResponse;
import org.example.new_new_mvp.dto.LoginRequest;
import org.example.new_new_mvp.dto.RegisterRequest;
import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.model.UserRole;
import org.example.new_new_mvp.model.Company;
import org.example.new_new_mvp.repository.UserRepository;
import org.example.new_new_mvp.repository.CompanyRepository;
import org.example.new_new_mvp.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;


@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public AuthResponse register(RegisterRequest request) {
        System.out.println("Registering user: " + request.getEmail() + " with role: " + request.getRole());
        
        try {
            // Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                System.out.println("Registration failed: Email already exists - " + request.getEmail());
                return new AuthResponse(null, null, null, null, null, "Email already exists");
            }
            
            User user = new User();
            user.setId(UUID.randomUUID()); // Generate UUID for Supabase
            user.setEmail(request.getEmail());
            user.setFullName(request.getFullName());
            user.setRole(request.getRole());
            
            // Handle company association
            if (request.getRole() == UserRole.COMPANY) {
                // Create new company for company user
                Company company = new Company();
                company.setId(UUID.randomUUID()); // Generate UUID for Supabase
                company.setName(request.getCompanyName() != null ? request.getCompanyName() : request.getFullName() + " Company");
                company = companyRepository.save(company);
                user.setCompany(company);
                System.out.println("Created company: " + company.getName() + " for user: " + request.getEmail());
            } else if (request.getCompanyId() != null && !request.getCompanyId().isEmpty()) {
                Company company = companyRepository.findById(UUID.fromString(request.getCompanyId()))
                        .orElseThrow(() -> new RuntimeException("Company not found"));
                user.setCompany(company);
                System.out.println("Associated user with company: " + company.getName());
            }
            
            System.out.println("Saving user to Supabase: " + user.getEmail());
            User savedUser = userRepository.save(user);
            System.out.println("User saved successfully: " + savedUser.getEmail());
            
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
            
            return new AuthResponse(token, user.getEmail(), user.getRole(), 
                                   user.getFullName(), user.getCompany() != null ? user.getCompany().getName() : null, 
                                   "Registration successful");
        } catch (Exception e) {
            System.out.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            return new AuthResponse(null, null, null, null, null, "Registration failed: " + e.getMessage());
        }
    }
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        
        if (user == null) {
            return new AuthResponse(null, null, null, null, null, "User not found");
        }
        
        // Since we're using OAuth, we don't need password validation
        // In real implementation, you would validate OAuth token here
        
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        
        return new AuthResponse(token, user.getEmail(), user.getRole(), 
                               user.getFullName(), user.getCompany() != null ? user.getCompany().getName() : null, 
                               "Login successful");
    }
    
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
