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
        
        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse(null, null, null, null, null, "Email already exists");
        }
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());
        
        // Handle company association
        if (request.getRole() == UserRole.COMPANY) {
            // Create new company for company user
            Company company = new Company();
            company.setName(request.getFullName() + " Company"); // Use full name as company name
            company = companyRepository.save(company);
            user.setCompany(company);
            System.out.println("Created company: " + company.getName() + " for user: " + request.getEmail());
        } else if (request.getCompanyId() != null) {
            Company company = companyRepository.findById(UUID.fromString(request.getCompanyId()))
                    .orElseThrow(() -> new RuntimeException("Company not found"));
            user.setCompany(company);
        }
        
        userRepository.save(user);
        
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        
        return new AuthResponse(token, user.getEmail(), user.getRole(), 
                               user.getFullName(), user.getCompany() != null ? user.getCompany().getName() : null, 
                               "Registration successful");
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
