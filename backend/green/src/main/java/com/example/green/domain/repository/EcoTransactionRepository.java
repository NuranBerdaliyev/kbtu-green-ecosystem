package com.example.green.domain.repository;

import com.example.green.domain.entity.EcoTransaction;
import com.example.green.domain.enums.EcoTransactionSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EcoTransactionRepository extends JpaRepository<EcoTransaction, Long>{
    Page<EcoTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<EcoTransaction> findByUserIdAndSourceAndReferenceId(Long userId, EcoTransactionSource source, Long referenceId);
    long countByUserId(Long userId);
    long countByUserIdAndSource(Long userId, EcoTransactionSource source);
}