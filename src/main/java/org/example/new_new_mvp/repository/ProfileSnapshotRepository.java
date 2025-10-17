package org.example.new_new_mvp.repository;

import org.example.new_new_mvp.model.ProfileSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProfileSnapshotRepository extends JpaRepository<ProfileSnapshot, UUID> {
    
    List<ProfileSnapshot> findByUserIdOrderBySnapshotDateDesc(UUID userId);
    
    List<ProfileSnapshot> findByUserCompanyId(UUID companyId);
    
    @Query("SELECT ps FROM ProfileSnapshot ps WHERE ps.user.id = :userId ORDER BY ps.snapshotDate DESC")
    List<ProfileSnapshot> findLatestSnapshotsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT ps FROM ProfileSnapshot ps WHERE ps.snapshotDate BETWEEN :startDate AND :endDate")
    List<ProfileSnapshot> findSnapshotsBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
