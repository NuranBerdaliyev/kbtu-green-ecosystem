package com.example.green.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WasteLogService {
    private final WasteLogRepository wasteLogRepository;
    private final UserRepository userRepository;
    private final EcoPointContainerRepository ecoPointContainerRepository;
    private final WasteLogMapper wasteLogMapper;

    public List<WasteLogResponseDto> findAll() {
        return wasteLogRepository.findAll().stream()
                .map(wasteLogMapper::toDto)
                .toList();
    }

    public WasteLogResponseDto findById(Long id) {
        return wasteLogMapper.toDto(getWasteLogOrThrow(id));
    }

    public WasteLogResponseDto create(WasteLogRequestDto request) {
        User user = getUserOrThrow(request.getUserId());
        EcoPointContainer ecoPointContainer = getEcoPointOrThrow(request.getEcoPointContainerId());
        WasteLog saved = wasteLogRepository.save(wasteLogMapper.toEntity(request, user, ecoPointContainer));
        return wasteLogMapper.toDto(saved);
    }

    public WasteLogResponseDto update(Long id, WasteLogRequestDto request) {
        WasteLog entity = getWasteLogOrThrow(id);
        User user = getUserOrThrow(request.getUserId());
        EcoPointContainer ecoPointContainer = getEcoPointOrThrow(request.getEcoPointContainerId());
        wasteLogMapper.updateEntity(entity, request, user, ecoPointContainer);
        WasteLog saved = wasteLogRepository.save(entity);
        return wasteLogMapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!wasteLogRepository.existsById(id)) {
            throw new ResourceNotFoundException("WasteLog not found: id=" + id);
        }
        wasteLogRepository.deleteById(id);
    }

    private WasteLog getWasteLogOrThrow(Long id) {
        return wasteLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WasteLog not found: id=" + id));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + id));
    }

    private EcoPointContainer getEcoPointOrThrow(Long id) {
        return ecoPointContainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EcoPointContainer not found: id=" + id));
    }
}

