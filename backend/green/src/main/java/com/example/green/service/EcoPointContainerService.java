package com.example.green.service;

import com.example.green.api.dto.request.EcoPointContainerRequestDto;
import com.example.green.api.dto.response.EcoPointContainerResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.EcoPointContainerMapper;
import com.example.green.domain.entity.EcoPointContainer;
import com.example.green.domain.repository.EcoPointContainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EcoPointContainerService {
    private final EcoPointContainerRepository ecoPointContainerRepository;
    private final EcoPointContainerMapper ecoPointContainerMapper;

    public List<EcoPointContainerResponseDto> findAll() {
        return ecoPointContainerRepository.findAll().stream()
                .map(ecoPointContainerMapper::toDto)
                .toList();
    }

    public EcoPointContainerResponseDto findById(Long id) {
        return ecoPointContainerMapper.toDto(getOrThrow(id));
    }

    public EcoPointContainerResponseDto create(EcoPointContainerRequestDto request) {
        EcoPointContainer saved = ecoPointContainerRepository.save(ecoPointContainerMapper.toEntity(request));
        return ecoPointContainerMapper.toDto(saved);
    }

    public EcoPointContainerResponseDto update(Long id, EcoPointContainerRequestDto request) {
        EcoPointContainer entity = getOrThrow(id);
        ecoPointContainerMapper.updateEntity(entity, request);
        EcoPointContainer saved = ecoPointContainerRepository.save(entity);
        return ecoPointContainerMapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!ecoPointContainerRepository.existsById(id)) {
            throw new ResourceNotFoundException("EcoPointContainer not found: id=" + id);
        }
        ecoPointContainerRepository.deleteById(id);
    }

    private EcoPointContainer getOrThrow(Long id) {
        return ecoPointContainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EcoPointContainer not found: id=" + id));
    }
}
