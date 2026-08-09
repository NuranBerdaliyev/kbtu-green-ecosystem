package com.example.green.api;

import com.example.green.domain.entity.JobApplication;
import com.example.green.service.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/job-applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService service;
    /*
    @PostMapping
    public ResponseEntity<JobApplication> create(@RequestBody @Valid JobApplication request) {
        //return ResponseEntity.ok(service.create(request));
    }
    */
}
