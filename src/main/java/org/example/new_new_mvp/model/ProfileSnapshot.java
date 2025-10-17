package org.example.new_new_mvp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "profile_snapshots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSnapshot {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "snapshot_date")
    private LocalDateTime snapshotDate = LocalDateTime.now();
    
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "profile_data")
    private String profileData;
    
    @PrePersist
    public void prePersist() {
        if (snapshotDate == null) {
            snapshotDate = LocalDateTime.now();
        }
    }
}