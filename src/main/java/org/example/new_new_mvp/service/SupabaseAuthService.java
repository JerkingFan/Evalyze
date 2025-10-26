package org.example.new_new_mvp.service;

import org.example.new_new_mvp.dto.AuthResponse;
import org.example.new_new_mvp.dto.LoginRequest;
import org.example.new_new_mvp.dto.RegisterRequest;
import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.model.UserRole;
import org.example.new_new_mvp.model.Company;
import org.example.new_new_mvp.repository.SupabaseUserRepository;
import org.example.new_new_mvp.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
public class SupabaseAuthService {
    
    @Autowired
    private SupabaseUserRepository userRepository;
    
    @Autowired
    private SupabaseService supabaseService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public Mono<AuthResponse> register(RegisterRequest request) {
        System.out.println("Registering user: " + request.getEmail() + " with role: " + request.getRole());
        
        return userRepository.existsByEmail(request.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.just(new AuthResponse(null, null, null, null, null, "Email already exists"));
                    }
                    
                    User user = new User();
                    user.setEmail(request.getEmail());
                    user.setFullName(request.getFullName());
                    user.setRole(request.getRole());
                    
                    // Handle company association
                    if (request.getRole() == UserRole.COMPANY) {
                        // Create new company for company user
                        Company company = new Company();
                        company.setName(request.getFullName() + " Company");
                        return supabaseService.insert("companies", company, Company.class)
                                .flatMap(savedCompany -> {
                                    user.setCompany(savedCompany);
                                    return userRepository.save(user);
                                })
                                .map(savedUser -> {
                                    User savedUserObj = (User) savedUser;
                                    String token = jwtUtil.generateToken(savedUserObj.getEmail(), savedUserObj.getRole().name());
                                    return new AuthResponse(token, savedUserObj.getEmail(), 
                                            savedUserObj.getRole(), savedUserObj.getFullName(), 
                                            savedUserObj.getCompany() != null ? savedUserObj.getCompany().getName() : null, null);
                                });
                    } else if (request.getCompanyId() != null) {
                        // Find existing company
                        return supabaseService.select("companies", Company.class, 
                                Map.of("id", "eq." + request.getCompanyId()))
                                .flatMap(companies -> {
                                    if (companies.isEmpty()) {
                                        return Mono.just(new AuthResponse(null, null, null, null, null, "Company not found"));
                                    }
                                    user.setCompany(companies.get(0));
                                    return userRepository.save(user);
                                })
                                .map(savedUser -> {
                                    User savedUserObj = (User) savedUser;
                                    String token = jwtUtil.generateToken(savedUserObj.getEmail(), savedUserObj.getRole().name());
                                    return new AuthResponse(token, savedUserObj.getEmail(), 
                                            savedUserObj.getRole(), savedUserObj.getFullName(), 
                                            savedUserObj.getCompany() != null ? savedUserObj.getCompany().getName() : null, null);
                                });
                    } else {
                        // No company association
                        return userRepository.save(user)
                                .map(savedUser -> {
                                    String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());
                                    return new AuthResponse(token, savedUser.getEmail(), 
                                            savedUser.getRole(), savedUser.getFullName(), null, null);
                                });
                    }
                });
    }
    
    public Mono<AuthResponse> login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .map(optionalUser -> {
                    if (optionalUser.isPresent()) {
                        User user = optionalUser.get();
                        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
                        return new AuthResponse(token, user.getEmail(), 
                                user.getRole(), user.getFullName(), 
                                user.getCompany() != null ? user.getCompany().getName() : null, null);
                    } else {
                        return new AuthResponse(null, null, null, null, null, "Invalid credentials");
                    }
                });
    }
    
    public Mono<User> getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .map(optionalUser -> optionalUser.orElse(null));
    }
}