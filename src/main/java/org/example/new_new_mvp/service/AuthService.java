package org.example.new_new_mvp.service;

import org.example.new_new_mvp.dto.ActivationCodeLoginRequest;
import org.example.new_new_mvp.dto.AuthResponse;
import org.example.new_new_mvp.dto.CreateEmployeeRequest;
import org.example.new_new_mvp.dto.LoginRequest;
import org.example.new_new_mvp.dto.RegisterRequest;
import org.example.new_new_mvp.model.User;
import org.example.new_new_mvp.model.UserRole;
import org.example.new_new_mvp.model.Company;
import org.example.new_new_mvp.repository.UserRepository;
import org.example.new_new_mvp.repository.CompanyRepository;
import org.example.new_new_mvp.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;


@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Регистрация ТОЛЬКО для компаний
     */
    public AuthResponse register(RegisterRequest request) {
        System.out.println("Registering user: " + request.getEmail() + " with role: " + request.getRole());
        System.out.println("Request data: email=" + request.getEmail() + ", fullName=" + request.getFullName() + ", companyName=" + request.getCompanyName());
        
        try {
            // ТОЛЬКО компании могут регистрироваться
            if (request.getRole() != UserRole.COMPANY) {
                System.out.println("Registration failed: Only companies can register");
                return new AuthResponse(null, null, null, null, null, "Only companies can register. Employees must use activation code.");
            }
            
            // Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                System.out.println("Registration failed: Email already exists - " + request.getEmail());
                return new AuthResponse(null, null, null, null, null, "Email already exists");
            }
            
            // Проверяем пароль
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return new AuthResponse(null, null, null, null, null, "Password is required");
            }
            
            // Создаем компанию
            String companyName = request.getCompanyName();
            if (companyName == null || companyName.trim().isEmpty()) {
                companyName = request.getFullName() + " Company";
            }
            
            Company company = new Company();
            company.setId(UUID.randomUUID());
            company.setName(companyName);
            company = companyRepository.save(company);
            System.out.println("Created company: " + company.getName());
            
            // Создаем пользователя-компанию
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmail(request.getEmail());
            user.setFullName(request.getFullName());
            user.setRole(UserRole.COMPANY);
            user.setCompany(company);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setActivationCode(UUID.randomUUID().toString()); // Генерируем уникальный код
            user.setCreatedAt(OffsetDateTime.now());
            user.setStatus("company"); // Статус 'company' для определения роли при чтении из БД
            
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
        System.out.println("Login attempt for email: " + request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        
        if (user == null) {
            System.out.println("User not found: " + request.getEmail());
            return new AuthResponse(null, null, null, null, null, "User not found");
        }
        
        System.out.println("User found: " + user.getEmail() + ", role: " + user.getRole());
        
        // Проверяем пароль для компаний
        if (user.getRole() == UserRole.COMPANY) {
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                System.out.println("Company has no password set");
                return new AuthResponse(null, null, null, null, null, "Invalid credentials");
            }
            
            // Проверяем пароль
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                System.out.println("Invalid password for company: " + user.getEmail());
                return new AuthResponse(null, null, null, null, null, "Invalid credentials");
            }
            
            System.out.println("Password validated successfully");
        } else {
            // Сотрудники должны входить по activation_code
            System.out.println("Employee trying to login with password - should use activation code");
            return new AuthResponse(null, null, null, null, null, "Employees must use activation code to login");
        }
        
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        
        System.out.println("Login successful for: " + user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getRole(), 
                               user.getFullName(), user.getCompany() != null ? user.getCompany().getName() : null, 
                               "Login successful");
    }
    
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    /**
     * Создание сотрудника компанией с генерацией activation_code
     */
    public AuthResponse createEmployee(CreateEmployeeRequest request, String companyEmail) {
        System.out.println("Creating employee: " + request.getEmail() + " for company: " + companyEmail);
        
        try {
            // Находим компанию
            User companyUser = userRepository.findByEmail(companyEmail)
                    .orElseThrow(() -> new RuntimeException("Company not found"));
            
            if (companyUser.getRole() != UserRole.COMPANY) {
                return new AuthResponse(null, null, null, null, null, "Only companies can create employees");
            }
            
            // Проверяем, что email сотрудника не занят
            if (userRepository.existsByEmail(request.getEmail())) {
                return new AuthResponse(null, null, null, null, null, "Email already exists");
            }
            
            // Создаем сотрудника
            User employee = new User();
            employee.setId(UUID.randomUUID());
            employee.setEmail(request.getEmail());
            employee.setFullName(request.getFullName());
            employee.setTelegramChatId(request.getTelegramChatId());
            employee.setRole(UserRole.EMPLOYEE);
            employee.setCompany(companyUser.getCompany());
            employee.setActivationCode(UUID.randomUUID().toString()); // Генерируем уникальный код
            employee.setCreatedAt(OffsetDateTime.now());
            employee.setStatus("invited"); // Сотрудник в статусе "приглашен"
            
            System.out.println("Saving employee to Supabase: " + employee.getEmail());
            User savedEmployee = userRepository.save(employee);
            System.out.println("Employee saved successfully with activation_code: " + savedEmployee.getActivationCode());
            
            return new AuthResponse(null, savedEmployee.getEmail(), savedEmployee.getRole(), 
                                   savedEmployee.getFullName(), savedEmployee.getActivationCode(), 
                                   "Employee created successfully. Activation code: " + savedEmployee.getActivationCode());
        } catch (Exception e) {
            System.out.println("Employee creation error: " + e.getMessage());
            e.printStackTrace();
            return new AuthResponse(null, null, null, null, null, "Employee creation failed: " + e.getMessage());
        }
    }
    
    /**
     * Вход сотрудника по activation_code
     */
    public AuthResponse loginByActivationCode(ActivationCodeLoginRequest request) {
        System.out.println("Login by activation code: " + request.getActivationCode());
        
        try {
            // Находим пользователя по activation_code
            User user = userRepository.findByActivationCode(request.getActivationCode())
                    .orElseThrow(() -> new RuntimeException("Invalid activation code"));
            
            // Обновляем статус на "active" при первом входе
            if ("invited".equals(user.getStatus())) {
                user.setStatus("active");
                user = userRepository.save(user);
                System.out.println("Employee activated: " + user.getEmail());
            }
            
            // Генерируем JWT токен
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole() != null ? user.getRole().name() : "EMPLOYEE");
            
            return new AuthResponse(token, user.getEmail(), user.getRole(), 
                                   user.getFullName(), user.getCompany() != null ? user.getCompany().getName() : null, 
                                   "Login successful");
        } catch (Exception e) {
            System.out.println("Login by activation code error: " + e.getMessage());
            e.printStackTrace();
            return new AuthResponse(null, null, null, null, null, "Login failed: " + e.getMessage());
        }
    }
}
