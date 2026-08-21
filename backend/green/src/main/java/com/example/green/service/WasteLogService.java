package com.example.green.service;

import com.example.green.api.dto.response.WasteLogResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.WasteLogMapper;
import com.example.green.domain.entity.WasteLog;
import com.example.green.domain.repository.WasteLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WasteLogService {
    private final WasteLogRepository wasteLogRepository;
    private final WasteLogMapper wasteLogMapper;

    @Transactional(readOnly = true)
    public List<WasteLogResponseDto> findAll() {
        return wasteLogRepository.findAll().stream()
                .map(wasteLogMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public WasteLogResponseDto findById(Long id) {
        return wasteLogMapper.toDto(getWasteLogOrThrow(id));
    }

    private WasteLog getWasteLogOrThrow(Long id) {
        return wasteLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WasteLog not found: id=" + id));
    }
}