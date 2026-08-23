package com.example.green.api.controller;

import com.example.green.api.dto.request.VacancyRequestDto;
import com.example.green.api.dto.request.VacancySearchRequestDto;
import com.example.green.api.dto.response.VacancyResponseDto;
import com.example.green.service.VacancyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/career/vacancies")
@RequiredArgsConstructor
@Validated
public class VacancyController {
    private final VacancyService vacancyService;

    @GetMapping
    public Page<VacancyResponseDto> search(@Valid @ModelAttribute VacancySearchRequestDto request) {
        return vacancyService.search(request);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('HR')")
    public List<VacancyResponseDto> myVacancies() {
        return vacancyService.findMyVacancies();
    }

    @GetMapping("/{id}")
    public VacancyResponseDto findById(@PathVariable @Positive Long id) {
        return vacancyService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('HR')")
    public VacancyResponseDto create(@Valid @RequestBody VacancyRequestDto request) {
        return vacancyService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public VacancyResponseDto update(@PathVariable @Positive Long id, @Valid @RequestBody VacancyRequestDto request) {
        return vacancyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('HR')")
    public void delete(@PathVariable @Positive Long id) {
        vacancyService.delete(id);
    }
}