package com.example.green.api.controller;

import com.example.green.api.dto.response.WasteLogResponseDto;
import com.example.green.service.EcoPointsContainerActionService;
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
    private final EcoPointsContainerActionService actionService;

    @GetMapping
    public List<WasteLogResponseDto> findAll() {
        return wasteLogService.findAll();
    }

    @GetMapping("/pending")
    public List<WasteLogResponseDto> findPending() {
        return wasteLogService.findPending();
    }

    @GetMapping("/{id}")
    public WasteLogResponseDto findById(@PathVariable @Positive Long id) {
        return wasteLogService.findById(id);
    }

    @PostMapping("/{id}/approve")
    public WasteLogResponseDto approve(@PathVariable @Positive Long id) {
        return actionService.approveDeposit(id);
    }

    @PostMapping("/{id}/reject")
    public WasteLogResponseDto reject(@PathVariable @Positive Long id) {
        return actionService.rejectDeposit(id);
    }
}