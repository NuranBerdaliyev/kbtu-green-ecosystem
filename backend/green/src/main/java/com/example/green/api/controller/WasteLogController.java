package com.example.green.api.controller;

import com.example.green.api.dto.response.WasteLogResponseDto;
import com.example.green.service.WasteLogService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/waste-logs")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class WasteLogController {
    private final WasteLogService wasteLogService;

    @GetMapping
    public List<WasteLogResponseDto> findAll() {
        return wasteLogService.findAll();
    }

    @GetMapping("/{id}")
    public WasteLogResponseDto findById(@PathVariable @Positive Long id) {
        return wasteLogService.findById(id);
    }
}