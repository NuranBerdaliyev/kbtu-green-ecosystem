package com.example.green.api.controller;

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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-applications")
@RequiredArgsConstructor
@Validated
public class JobApplicationController {
    private final JobApplicationRepository jobApplicationRepository;
    private final VacancyRepository vacancyRepository;
    private final UserRepository userRepository;
    private final JobApplicationMapper jobApplicationMapper;

    @GetMapping
    public List<JobApplicationResponseDto> findAll() {
        return jobApplicationRepository.findAll().stream()
                .map(jobApplicationMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public JobApplicationResponseDto findById(@PathVariable @Positive Long id) {
        JobApplication entity = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication not found: id=" + id));
        return jobApplicationMapper.toDto(entity);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplicationResponseDto create(@RequestBody @Valid JobApplicationRequestDto request) {
        Vacancy vacancy = vacancyRepository.findById(request.getVacancyId())
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found: id=" + request.getVacancyId()));

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + request.getStudentId()));

        JobApplication saved = jobApplicationRepository.save(jobApplicationMapper.toEntity(request, vacancy, student));
        return jobApplicationMapper.toDto(saved);
    }

    @PutMapping("/{id}")
    public JobApplicationResponseDto update(@PathVariable @Positive Long id,
                                            @RequestBody @Valid JobApplicationRequestDto request) {
        JobApplication entity = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication not found: id=" + id));

        Vacancy vacancy = vacancyRepository.findById(request.getVacancyId())
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found: id=" + request.getVacancyId()));

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + request.getStudentId()));

        jobApplicationMapper.updateEntity(entity, request, vacancy, student);
        JobApplication saved = jobApplicationRepository.save(entity);
        return jobApplicationMapper.toDto(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        if (!jobApplicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("JobApplication not found: id=" + id);
        }
        jobApplicationRepository.deleteById(id);
    }
}
