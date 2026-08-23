package com.example.green.api.controller;

import com.example.green.api.dto.request.EcoPointContainerRequestDto;
import com.example.green.api.dto.response.EcoPointContainerResponseDto;
import com.example.green.service.EcoPointContainerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eco-point-containers")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class EcoPointContainerController {
    private final EcoPointContainerService ecoPointContainerService;

    @GetMapping
    public List<EcoPointContainerResponseDto> findAll() {
        return ecoPointContainerService.findAll();
    }

    @GetMapping("/{id}")
    public EcoPointContainerResponseDto findById(@PathVariable @Positive Long id) {
        return ecoPointContainerService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EcoPointContainerResponseDto create(@RequestBody @Valid EcoPointContainerRequestDto request) {
        return ecoPointContainerService.create(request);
    }

    @PostMapping("/{id}/empty")
    public EcoPointContainerResponseDto empty(
            @PathVariable @Positive Long id
    ) {
        return ecoPointContainerService.empty(id);
    }

    @PutMapping("/{id}")
    public EcoPointContainerResponseDto update(@PathVariable @Positive Long id,
                                               @RequestBody @Valid EcoPointContainerRequestDto request) {
        return ecoPointContainerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        ecoPointContainerService.delete(id);
    }
}
