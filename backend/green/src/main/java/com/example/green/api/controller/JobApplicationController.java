package com.example.green.api.controller;

import com.example.green.api.dto.request.JobApplicationRequestDto;
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
@RequestMapping("/api/job-applications")
@RequiredArgsConstructor
@Validated
public class JobApplicationController {
    private final JobApplicationService jobApplicationService;

    @GetMapping
    public List<JobApplicationResponseDto> findAll() {
        return jobApplicationService.findAll();
    }

    @GetMapping("/{id}")
    public JobApplicationResponseDto findById(@PathVariable @Positive Long id) {
        return jobApplicationService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplicationResponseDto create(@RequestBody @Valid JobApplicationRequestDto request) {
        return jobApplicationService.create(request);
    }

    @PutMapping("/{id}")
    public JobApplicationResponseDto update(@PathVariable @Positive Long id,
                                            @RequestBody @Valid JobApplicationRequestDto request) {
        return jobApplicationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        jobApplicationService.delete(id);
    }
}
