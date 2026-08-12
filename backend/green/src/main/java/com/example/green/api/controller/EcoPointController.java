package com.example.green.api.controller;

import com.example.green.api.dto.request.EcoPointContainerRequestDto;
import com.example.green.api.dto.response.EcoPointContainerResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.EcoPointContainerMapper;
import com.example.green.domain.entity.EcoPointContainer;
import com.example.green.domain.repository.EcoPointContainerRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eco-point-containers")
@RequiredArgsConstructor
@Validated
public class EcoPointController {
    private final EcoPointContainerRepository ecoPointContainerRepository;
    private final EcoPointContainerMapper ecoPointContainerMapper;

    @GetMapping
    public List<EcoPointContainerResponseDto> findAll() {
        return ecoPointContainerRepository.findAll().stream()
                .map(ecoPointContainerMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public EcoPointContainerResponseDto findById(@PathVariable @Positive Long id) {
        EcoPointContainer entity = ecoPointContainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EcoPointContainer not found: id=" + id));
        return ecoPointContainerMapper.toDto(entity);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EcoPointContainerResponseDto create(@RequestBody @Valid EcoPointContainerRequestDto request) {
        EcoPointContainer saved = ecoPointContainerRepository.save(ecoPointContainerMapper.toEntity(request));
        return ecoPointContainerMapper.toDto(saved);
    }

    @PutMapping("/{id}")
    public EcoPointContainerResponseDto update(@PathVariable @Positive Long id,
                                               @RequestBody @Valid EcoPointContainerRequestDto request) {
        EcoPointContainer entity = ecoPointContainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EcoPointContainer not found: id=" + id));

        ecoPointContainerMapper.updateEntity(entity, request);
        EcoPointContainer saved = ecoPointContainerRepository.save(entity);
        return ecoPointContainerMapper.toDto(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        if (!ecoPointContainerRepository.existsById(id)) {
            throw new ResourceNotFoundException("EcoPointContainer not found: id=" + id);
        }
        ecoPointContainerRepository.deleteById(id);
    }
}
