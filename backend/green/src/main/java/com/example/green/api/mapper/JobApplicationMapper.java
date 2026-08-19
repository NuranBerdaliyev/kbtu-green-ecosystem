package com.example.green.api.mapper;

import com.example.green.api.dto.response.CandidateResponseDto;
import com.example.green.api.dto.response.JobApplicationResponseDto;
import com.example.green.domain.entity.JobApplication;
import com.example.green.domain.entity.User;
import com.example.green.domain.entity.Vacancy;
import com.example.green.domain.enums.JobStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class JobApplicationMapper {

    public JobApplication toEntity(
            String coverLetter,
            Vacancy vacancy,
            User student
    ) {
        return JobApplication.builder()
                .vacancy(vacancy)
                .student(student)
                .appliedAt(LocalDateTime.now())
                .coverLetter(coverLetter)
                .jobStatus(JobStatus.PENDING)
                .build();
    }

    public JobApplicationResponseDto toDto(
            JobApplication entity
    ) {
        return JobApplicationResponseDto.builder()
                .id(entity.getId())
                .vacancyId(entity.getVacancy().getId())
                .vacancyTitle(entity.getVacancy().getTitle())
                .companyId(
                        entity.getVacancy()
                                .getCompany()
                                .getId()
                )
                .companyName(
                        entity.getVacancy()
                                .getCompany()
                                .getName()
                )
                .appliedAt(entity.getAppliedAt())
                .coverLetter(entity.getCoverLetter())
                .jobStatus(entity.getJobStatus())
                .build();
    }

    public CandidateResponseDto toCandidateDto(JobApplication entity) {
        User student = entity.getStudent();
        return CandidateResponseDto.builder()
                .applicationId(entity.getId())
                .studentId(student.getId())
                .fullName(student.getFullName())
                .esgRating(student.getEsgRating())
                .ecoCoinsBalance(student.getEcoCoinsBalance())
                .totalCo2Saved(student.getTotalCo2Saved())
                .coverLetter(entity.getCoverLetter())
                .appliedAt(entity.getAppliedAt())
                .jobStatus(entity.getJobStatus())
                .build();
    }
}