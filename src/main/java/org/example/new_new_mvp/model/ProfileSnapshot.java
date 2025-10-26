package org.example.new_new_mvp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "profile_snapshots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSnapshot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id")
    private UUID userId;
    
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "user_id")
    // private User user;
    
    @Column(name = "snapshot_date")
    private OffsetDateTime snapshotDate;
    
    @Column(name = "profile_data", columnDefinition = "TEXT")
    private Object profileData;
}