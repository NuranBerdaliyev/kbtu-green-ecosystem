package com.example.green.domain.repository;

import com.example.green.domain.entity.EcoPointContainer;
import com.example.green.domain.enums.WasteType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EcoPointContainerRepository extends JpaRepository<EcoPointContainer, Long> {
    List<EcoPointContainer> findByWasteTypeAndIsActive(WasteType wasteType, Boolean isActive);
    // 新增：获取所有激活状态的回收箱
    List<EcoPointContainer> findByIsActiveTrue();
    // 新增：根据二维码 token 查询回收箱
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select c
        from EcoPointContainer c
        where c.qrCodeToken = :qrCodeToken
        """)
    Optional<EcoPointContainer> findByQrCodeTokenForUpdate(@Param("qrCodeToken") String qrCodeToken);
    long countByIsActiveTrue();
    long countByIsActiveTrueAndFullnessPercentageGreaterThanEqual(Integer fullnessPercentage);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select c
        from EcoPointContainer c
        where c.id = :id
        """)
    Optional<EcoPointContainer> findByIdForUpdate(@Param("id") Long id);
}