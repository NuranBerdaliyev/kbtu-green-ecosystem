package com.example.green.domain.repository;

import com.example.green.domain.entity.JobApplication;
import com.example.green.domain.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByStudentId(Long studentId);
    List<JobApplication> findByVacancyId(Long vacancyId);
    List<JobApplication> findByJobStatus(JobStatus jobStatus);
}