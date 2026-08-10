package com.example.green.api.controller;

import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.domain.entity.JobApplication;
import com.example.green.domain.repository.JobApplicationRepository;
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
    private final JobApplicationRepository repository;

    @GetMapping
    public List<JobApplication> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public JobApplication findById(@PathVariable @Positive Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication not found: id=" + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplication create(@RequestBody @Valid JobApplication request) {
        request.setId(null);
        return repository.save(request);
    }

    @PutMapping("/{id}")
    public JobApplication update(@PathVariable @Positive Long id, @RequestBody @Valid JobApplication request) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("JobApplication not found: id=" + id);
        }
        request.setId(id);
        return repository.save(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("JobApplication not found: id=" + id);
        }
        repository.deleteById(id);
    }
}
