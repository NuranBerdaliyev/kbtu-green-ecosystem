package com.example.green.api.controller;

import com.example.green.api.dto.request.JobApplicationRequestDto;
import com.example.green.api.dto.request.JobApplicationStatusRequestDto;
import com.example.green.api.dto.response.CandidateResponseDto;
import com.example.green.api.dto.response.JobApplicationResponseDto;
import com.example.green.service.JobApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/career")
@RequiredArgsConstructor
@Validated
public class JobApplicationController {
    private final JobApplicationService jobApplicationService;

    @PostMapping("/vacancies/{vacancyId}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplicationResponseDto apply(@PathVariable @Positive Long vacancyId, @Valid @RequestBody JobApplicationRequestDto request) {
        return jobApplicationService.apply(
                vacancyId,
                request
        );
    }

    @GetMapping("/applications/my")
    public List<JobApplicationResponseDto> myApplications() {
        return jobApplicationService
                .findMyApplications();
    }

    @GetMapping("/vacancies/{vacancyId}/applications")
    public List<CandidateResponseDto> candidates(@PathVariable @Positive Long vacancyId) {
        return jobApplicationService.findCandidates(vacancyId);
    }

    @PatchMapping("/applications/{applicationId}/status")
    public CandidateResponseDto changeStatus(@PathVariable @Positive Long applicationId, @Valid @RequestBody JobApplicationStatusRequestDto request) {
        return jobApplicationService.changeStatus(applicationId, request);
    }
}