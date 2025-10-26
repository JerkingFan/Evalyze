package org.example.new_new_mvp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @JsonProperty("created_at")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    
    @JsonProperty("full_name")
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @JsonProperty("telegram_chat_id")
    @Column(name = "telegram_chat_id")
    private String telegramChatId;
    
    @Column(nullable = false)
    private String status = "invited";
    
    @JsonProperty("activation_code")
    @Column(name = "activation_code", unique = true, nullable = false)
    private String activationCode;
    
    @JsonProperty("access_token")
    @Column(name = "access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    @Column(name = "refresh_token")
    private String refreshToken;
    
    @JsonProperty("token_expires_at")
    @Column(name = "token_expires_at")
    private OffsetDateTime tokenExpiresAt;
    
    @JsonProperty("last_updated")
    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;
    
    @JsonProperty("Role")
    @Column(name = "\"Role\"")
    private UUID roleId; // UUID ссылка на job_roles
    
    private String password;
    
    // Для обратной совместимости с кодом
    @Transient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    
    @Transient
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    
    // @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private Profile profile;
    
    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<ProfileSnapshot> profileSnapshots;
    
    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Определяем роль: если есть company (transient), то COMPANY, иначе EMPLOYEE
        if (role != null) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
        }
        // По умолчанию EMPLOYEE
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
    }
    
    @Override
    public String getPassword() {
        return password; // Теперь храним пароль для компаний
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}