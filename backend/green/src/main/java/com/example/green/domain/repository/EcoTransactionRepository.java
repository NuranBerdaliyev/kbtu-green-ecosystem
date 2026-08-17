package com.example.green.domain.repository;

import com.example.green.domain.entity.EcoTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface EcoTransactionRepository extends JpaRepository<EcoTransaction, Long>{
    List<EcoTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}