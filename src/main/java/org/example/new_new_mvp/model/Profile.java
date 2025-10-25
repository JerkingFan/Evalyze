package org.example.new_new_mvp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    
    @Id
    @Column(name = "user_id")
    private UUID userId;
    
    // @OneToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "user_id")
    // private User user;
    
    @Column(name = "profile_data", columnDefinition = "TEXT")
    private String profileData;
    
    @Enumerated(EnumType.STRING)
    private ProfileStatus status;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}