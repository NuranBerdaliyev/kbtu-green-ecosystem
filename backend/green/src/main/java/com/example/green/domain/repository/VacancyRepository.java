package com.example.green.domain.repository;

import com.example.green.domain.entity.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacancyRepository extends JpaRepository<Vacancy, Long> {
    List<Vacancy> findByHrManagerId(Long hrManagerId);
    List<Vacancy> findByIsPartnerVacancy(Boolean isPartnerVacancy);
}