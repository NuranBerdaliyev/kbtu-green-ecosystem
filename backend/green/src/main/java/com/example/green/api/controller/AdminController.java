package com.example.green.api.controller;

import com.example.green.api.dto.request.VacancyStatusRequestDto;
import com.example.green.api.dto.response.AdminDashboardResponseDto;
import com.example.green.api.dto.response.VacancyResponseDto;
import com.example.green.service.AdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public AdminDashboardResponseDto dashboard() {
        return adminService.getDashboard();
    }

    @GetMapping("/vacancies")
    public Page<VacancyResponseDto> vacancies(@RequestParam(defaultValue = "0") @Min(0) int page,
                                              @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return adminService.getAllVacancies(page, size);
    }

    @PatchMapping("/vacancies/{vacancyId}/status")
    public VacancyResponseDto changeVacancyStatus(@PathVariable @Positive Long vacancyId,
                                                  @Valid @RequestBody VacancyStatusRequestDto request) {
        return adminService.changeVacancyStatus(vacancyId, request);
    }
}