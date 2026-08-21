package com.example.green.domain.repository;

import com.example.green.domain.entity.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface VacancyRepository extends JpaRepository<Vacancy, Long>,
        JpaSpecificationExecutor<Vacancy> {
    List<Vacancy> findByHrManagerIdOrderByIdDesc(Long hrManagerId);
    boolean existsByCompanyId(Long companyId);
    Optional<Vacancy> findByIdAndIsActiveTrueAndCompany_IsPartnerTrue(Long id);
    long countByIsActiveTrue();
}