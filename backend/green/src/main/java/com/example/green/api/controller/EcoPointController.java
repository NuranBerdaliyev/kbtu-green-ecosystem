package com.example.green.api.controller;

import com.example.green.api.dto.request.EcoPointContainerRequestDto;
import com.example.green.api.dto.response.EcoPointContainerResponseDto;
import com.example.green.service.EcoPointContainerService;
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
