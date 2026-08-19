package com.example.green.domain.repository;

import com.example.green.domain.entity.EcoPointContainer;
import com.example.green.domain.enums.WasteType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EcoPointContainerRepository extends JpaRepository<EcoPointContainer, Long> {
    List<EcoPointContainer> findByWasteTypeAndIsActive(WasteType wasteType, Boolean isActive);

    // 新增：获取所有激活状态的回收箱
    List<EcoPointContainer> findByIsActiveTrue();

    // 新增：根据二维码 token 查询回收箱
    Optional<EcoPointContainer> findByQrCodeToken(String qrCodeToken);
}