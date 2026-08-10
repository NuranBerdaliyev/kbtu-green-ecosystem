package com.example.green.domain.repository;

import com.example.green.domain.entity.WasteLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WasteLogRepository extends JpaRepository<WasteLog, Long> {
    List<WasteLog> findByUserId(Long userId);
    List<WasteLog> findByEcoPointContainerId(Long ecoPointContainerId);
}