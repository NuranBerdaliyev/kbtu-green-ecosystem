package com.example.green.domain.repository;

import com.example.green.domain.entity.EcoPointContainer;
import com.example.green.domain.enums.WasteType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EcoPointContainerRepository extends JpaRepository<EcoPointContainer, Long> {
    List<EcoPointContainer> findByWasteTypeAndIsActive(WasteType wasteType, Boolean isActive);
}