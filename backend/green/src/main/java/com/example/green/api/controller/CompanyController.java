package com.example.green.api.controller;

import com.example.green.api.dto.request.CompanyPartnerStatusRequestDto;
import com.example.green.api.dto.request.CompanyRequestDto;
import com.example.green.api.dto.response.CompanyResponseDto;
import com.example.green.service.CompanyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/career/companies")
@RequiredArgsConstructor
@Validated
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public List<CompanyResponseDto> findAll() {
        return companyService.findAll();
    }

    @GetMapping("/{id}")
    public CompanyResponseDto findById(@PathVariable @Positive Long id) {
        return companyService.findById(id);
    }


    @GetMapping("/my")
    @PreAuthorize("hasRole('HR')")
    public List<CompanyResponseDto> myCompanies() {
        return companyService.findMyCompanies();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('HR')")
    public CompanyResponseDto create(@Valid @RequestBody CompanyRequestDto request) {
        return companyService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public CompanyResponseDto update(@PathVariable @Positive Long id, @Valid @RequestBody CompanyRequestDto request) {
        return companyService.update(id, request);
    }

    @PatchMapping("/{id}/partner-status")
    @PreAuthorize("hasRole('ADMIN')")
    public CompanyResponseDto changePartnerStatus(
            @PathVariable @Positive Long id,
            @Valid @RequestBody
            CompanyPartnerStatusRequestDto request
    ) {
        return companyService.changePartnerStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('HR')")
    public void delete(@PathVariable @Positive Long id) {
        companyService.delete(id);
    }
}