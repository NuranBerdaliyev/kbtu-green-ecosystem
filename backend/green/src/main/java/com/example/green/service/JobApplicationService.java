package com.example.green.service;

import com.example.green.api.dto.request.JobApplicationRequestDto;
import com.example.green.api.dto.response.JobApplicationResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.JobApplicationMapper;
import com.example.green.domain.entity.JobApplication;
import com.example.green.domain.entity.User;
import com.example.green.domain.entity.Vacancy;
import com.example.green.domain.repository.JobApplicationRepository;
import com.example.green.domain.repository.UserRepository;
import com.example.green.domain.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobApplicationService {
    private final JobApplicationRepository jobApplicationRepository;
    private final VacancyRepository vacancyRepository;
    private final UserRepository userRepository;
    private final JobApplicationMapper jobApplicationMapper;

    public List<JobApplicationResponseDto> findAll() {
        return jobApplicationRepository.findAll().stream()
                .map(jobApplicationMapper::toDto)
                .toList();
    }

    public JobApplicationResponseDto findById(Long id) {
        return jobApplicationMapper.toDto(getJobApplicationOrThrow(id));
    }

    public JobApplicationResponseDto create(JobApplicationRequestDto request) {
        Vacancy vacancy = getVacancyOrThrow(request.getVacancyId());
        User student = getUserOrThrow(request.getStudentId());
        JobApplication saved = jobApplicationRepository.save(jobApplicationMapper.toEntity(request, vacancy, student));
        return jobApplicationMapper.toDto(saved);
    }

    public JobApplicationResponseDto update(Long id, JobApplicationRequestDto request) {
        JobApplication entity = getJobApplicationOrThrow(id);
        Vacancy vacancy = getVacancyOrThrow(request.getVacancyId());
        User student = getUserOrThrow(request.getStudentId());
        jobApplicationMapper.updateEntity(entity, request, vacancy, student);
        JobApplication saved = jobApplicationRepository.save(entity);
        return jobApplicationMapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!jobApplicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("JobApplication not found: id=" + id);
        }
        jobApplicationRepository.deleteById(id);
    }

    private JobApplication getJobApplicationOrThrow(Long id) {
        return jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication not found: id=" + id));
    }

    private Vacancy getVacancyOrThrow(Long id) {
        return vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found: id=" + id));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + id));
    }
}
