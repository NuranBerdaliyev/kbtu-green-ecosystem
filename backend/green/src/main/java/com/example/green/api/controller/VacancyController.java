package com.example.green.api.controller;

import com.example.green.api.dto.request.VacancyRequestDto;
import com.example.green.api.dto.response.VacancyResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.VacancyMapper;
import com.example.green.domain.entity.User;
import com.example.green.domain.entity.Vacancy;
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
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
@Validated
public class VacancyController {
    private final VacancyRepository vacancyRepository;
    private final UserRepository userRepository;
    private final VacancyMapper vacancyMapper;

    @GetMapping
    public List<VacancyResponseDto> findAll() {
        return vacancyRepository.findAll().stream()
                .map(vacancyMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public VacancyResponseDto findById(@PathVariable @Positive Long id) {
        Vacancy entity = vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found: id=" + id));
        return vacancyMapper.toDto(entity);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VacancyResponseDto create(@RequestBody @Valid VacancyRequestDto request) {
        User hrManager = userRepository.findById(request.getHrManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + request.getHrManagerId()));

        Vacancy saved = vacancyRepository.save(vacancyMapper.toEntity(request, hrManager));
        return vacancyMapper.toDto(saved);
    }

    @PutMapping("/{id}")
    public VacancyResponseDto update(@PathVariable @Positive Long id,
                                     @RequestBody @Valid VacancyRequestDto request) {
        Vacancy entity = vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found: id=" + id));

        User hrManager = userRepository.findById(request.getHrManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + request.getHrManagerId()));

        vacancyMapper.updateEntity(entity, request, hrManager);
        Vacancy saved = vacancyRepository.save(entity);
        return vacancyMapper.toDto(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        if (!vacancyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vacancy not found: id=" + id);
        }
        vacancyRepository.deleteById(id);
    }
}

