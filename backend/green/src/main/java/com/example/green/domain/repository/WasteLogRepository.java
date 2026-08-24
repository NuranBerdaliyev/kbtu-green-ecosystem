package com.example.green.domain.repository;

import com.example.green.domain.entity.WasteLog;
import com.example.green.domain.enums.WasteDepositStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WasteLogRepository extends JpaRepository<WasteLog, Long> {
    List<WasteLog> findByUserId(Long userId);
    List<WasteLog> findByEcoPointContainerId(Long ecoPointContainerId);
    boolean existsByEcoPointContainerId(Long ecoPointContainerId);
    List<WasteLog> findByStatusOrderByScannedAtAsc(WasteDepositStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select log
        from WasteLog log
        where log.id = :id
        """)
    Optional<WasteLog> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        select coalesce(sum(log.wasteWeightGrams), 0)
        from WasteLog log
        where log.user.id = :userId
          and log.status = :status
          and log.reviewedAt >= :from
          and log.reviewedAt < :to
        """)
    Long sumWeightByUserAndStatusBetween(
            @Param("userId") Long userId,
            @Param("status") WasteDepositStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}