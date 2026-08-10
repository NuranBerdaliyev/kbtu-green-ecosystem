package com.example.green.api.controller;

import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.domain.entity.Vacancy;
import com.example.green.domain.repository.VacancyRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
@Validated
public class VacancyController {
    private final VacancyRepository repository;

    @GetMapping
    public List<Vacancy> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Vacancy findById(@PathVariable @Positive Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found: id=" + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vacancy create(@RequestBody @Valid Vacancy request) {
        request.setId(null);
        return repository.save(request);
    }

    @PutMapping("/{id}")
    public Vacancy update(@PathVariable @Positive Long id, @RequestBody @Valid Vacancy request) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Vacancy not found: id=" + id);
        }
        request.setId(id);
        return repository.save(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Vacancy not found: id=" + id);
        }
        repository.deleteById(id);
    }
}

