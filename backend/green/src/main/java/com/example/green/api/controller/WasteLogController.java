package com.example.green.api.controller;

import com.example.green.api.dto.request.WasteLogRequestDto;
import com.example.green.api.dto.response.WasteLogResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.WasteLogMapper;
import com.example.green.domain.entity.EcoPointContainer;
import com.example.green.domain.entity.User;
import com.example.green.domain.entity.WasteLog;
import com.example.green.domain.repository.EcoPointContainerRepository;
import com.example.green.domain.repository.UserRepository;
import com.example.green.domain.repository.WasteLogRepository;
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
    private final WasteLogRepository wasteLogRepository;
    private final UserRepository userRepository;
    private final EcoPointContainerRepository ecoPointContainerRepository;
    private final WasteLogMapper wasteLogMapper;

    @GetMapping
    public List<WasteLogResponseDto> findAll() {
        return wasteLogRepository.findAll().stream()
                .map(wasteLogMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public WasteLogResponseDto findById(@PathVariable @Positive Long id) {
        WasteLog entity = wasteLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WasteLog not found: id=" + id));
        return wasteLogMapper.toDto(entity);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WasteLogResponseDto create(@RequestBody @Valid WasteLogRequestDto request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + request.getUserId()));

        EcoPointContainer ecoPointContainer = ecoPointContainerRepository.findById(request.getEcoPointContainerId())
                .orElseThrow(() -> new ResourceNotFoundException("EcoPointContainer not found: id=" + request.getEcoPointContainerId()));

        WasteLog saved = wasteLogRepository.save(wasteLogMapper.toEntity(request, user, ecoPointContainer));
        return wasteLogMapper.toDto(saved);
    }

    @PutMapping("/{id}")
    public WasteLogResponseDto update(@PathVariable @Positive Long id,
                                      @RequestBody @Valid WasteLogRequestDto request) {
        WasteLog entity = wasteLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WasteLog not found: id=" + id));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + request.getUserId()));

        EcoPointContainer ecoPointContainer = ecoPointContainerRepository.findById(request.getEcoPointContainerId())
                .orElseThrow(() -> new ResourceNotFoundException("EcoPointContainer not found: id=" + request.getEcoPointContainerId()));

        wasteLogMapper.updateEntity(entity, request, user, ecoPointContainer);
        WasteLog saved = wasteLogRepository.save(entity);
        return wasteLogMapper.toDto(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        if (!wasteLogRepository.existsById(id)) {
            throw new ResourceNotFoundException("WasteLog not found: id=" + id);
        }
        wasteLogRepository.deleteById(id);
    }
}
