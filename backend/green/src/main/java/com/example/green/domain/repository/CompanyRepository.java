package com.example.green.domain.repository;
import com.example.green.domain.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByHrManagerIdOrderByNameAsc(Long hrManagerId);
    boolean existsByHrManagerIdAndNameIgnoreCase(Long hrManagerId, String name);
    long countByIsPartnerTrue();
}