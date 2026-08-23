package com.example.green.service;

import com.example.green.api.dto.request.EcoPointContainerRequestDto;
import com.example.green.api.dto.response.EcoPointContainerResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.EcoPointContainerMapper;
import com.example.green.domain.entity.EcoPointContainer;
import com.example.green.domain.repository.EcoPointContainerRepository;
import com.example.green.domain.repository.WasteLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EcoPointContainerService {
    private final EcoPointContainerRepository ecoPointContainerRepository;
    private final WasteLogRepository wasteLogRepository;
    private final EcoPointContainerMapper ecoPointContainerMapper;

    @Transactional(readOnly = true)
    public List<EcoPointContainerResponseDto> findAll() {
        return ecoPointContainerRepository.findAll().stream()
                .map(ecoPointContainerMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public EcoPointContainerResponseDto findById(Long id) {
        return ecoPointContainerMapper.toDto(getOrThrow(id));
    }

    @Transactional
    public EcoPointContainerResponseDto create(EcoPointContainerRequestDto request) {
        EcoPointContainer saved = ecoPointContainerRepository.save(ecoPointContainerMapper.toEntity(request));
        return ecoPointContainerMapper.toDto(saved);
    }

    @Transactional
    public EcoPointContainerResponseDto update(Long id, EcoPointContainerRequestDto request) {
        EcoPointContainer entity = getOrThrow(id);
        ecoPointContainerMapper.updateEntity(entity, request);
        return ecoPointContainerMapper.toDto(ecoPointContainerRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        EcoPointContainer container = getOrThrow(id);
        if (wasteLogRepository.existsByEcoPointContainerId(id)) {
            throw new IllegalStateException("Container with waste history cannot be deleted. Deactivate it instead");
        }
        ecoPointContainerRepository.deleteById(id);
    }

    private EcoPointContainer getOrThrow(Long id) {
        return ecoPointContainerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("EcoPointContainer not found: id=" + id));
    }
}