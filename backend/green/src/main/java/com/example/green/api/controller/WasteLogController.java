package com.example.green.api.controller;

import com.example.green.api.dto.request.WasteLogRequestDto;
import com.example.green.api.dto.response.WasteLogResponseDto;
import com.example.green.service.WasteLogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/waste-logs")
@RequiredArgsConstructor
@Validated
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WasteLogResponseDto create(@RequestBody @Valid WasteLogRequestDto request) {
        return wasteLogService.create(request);
    }

    @PutMapping("/{id}")
    public WasteLogResponseDto update(@PathVariable @Positive Long id,
                                      @RequestBody @Valid WasteLogRequestDto request) {
        return wasteLogService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        wasteLogService.delete(id);
    }
}
