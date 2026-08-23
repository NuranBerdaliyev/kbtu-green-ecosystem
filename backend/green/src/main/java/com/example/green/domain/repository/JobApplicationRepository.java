package com.example.green.domain.repository;

import com.example.green.domain.entity.JobApplication;
import com.example.green.domain.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByStudentIdOrderByAppliedAtDesc(Long studentId);
    List<JobApplication> findByVacancyIdOrderByAppliedAtDesc(Long vacancyId);
    List<JobApplication> findByVacancyIdOrderByStudent_EsgRatingDescAppliedAtAsc(Long vacancyId);
    List<JobApplication> findByJobStatus(JobStatus jobStatus);
    boolean existsByVacancyIdAndStudentId(Long vacancyId, Long studentId);
    boolean existsByVacancyId(Long vacancyId);
}