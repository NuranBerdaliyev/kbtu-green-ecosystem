package com.example.green.api.mapper;

import com.example.green.api.dto.request.JobApplicationRequestDto;
import com.example.green.api.dto.response.JobApplicationResponseDto;
import com.example.green.domain.entity.JobApplication;
import com.example.green.domain.entity.User;
import com.example.green.domain.entity.Vacancy;
import org.springframework.stereotype.Component;

@Component
public class JobApplicationMapper {
    public JobApplication toEntity(JobApplicationRequestDto dto, Vacancy vacancy, User student) {
        return JobApplication.builder()
                .vacancy(vacancy)
                .student(student)
                .appliedAt(dto.getAppliedAt())
                .coverLetter(dto.getCoverLetter())
                .jobStatus(dto.getJobStatus())
                .build();
    }

    public void updateEntity(JobApplication entity, JobApplicationRequestDto dto, Vacancy vacancy, User student) {
        entity.setVacancy(vacancy);
        entity.setStudent(student);
        entity.setAppliedAt(dto.getAppliedAt());
        entity.setCoverLetter(dto.getCoverLetter());
        entity.setJobStatus(dto.getJobStatus());
    }

    public JobApplicationResponseDto toDto(JobApplication entity) {
        return JobApplicationResponseDto.builder()
                .id(entity.getId())
                .vacancyId(entity.getVacancy().getId())
                .studentId(entity.getStudent().getId())
                .appliedAt(entity.getAppliedAt())
                .coverLetter(entity.getCoverLetter())
                .jobStatus(entity.getJobStatus())
                .build();
    }
}
