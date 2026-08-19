package com.example.green.domain.repository;

import com.example.green.domain.entity.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface VacancyRepository extends JpaRepository<Vacancy, Long>,
        JpaSpecificationExecutor<Vacancy> {
    List<Vacancy> findByHrManagerIdOrderByIdDesc(Long hrManagerId);
    boolean existsByCompanyId(Long companyId);
}