package com.example.green.service;

import com.example.green.api.dto.request.EcoPointContainerRequestDto;
import com.example.green.api.dto.response.EcoPointContainerResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.EcoPointContainerMapper;
import com.example.green.domain.entity.EcoPointContainer;
import com.example.green.domain.repository.EcoPointContainerRepository;
import com.example.green.domain.repository.WasteLogRepository;
import com.example.green.service.event.EcoPointContainerChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EcoPointContainerService {
    private final EcoPointContainerRepository ecoPointContainerRepository;
    private final WasteLogRepository wasteLogRepository;
    private final EcoPointContainerMapper ecoPointContainerMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

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
        EcoPointContainer entity = ecoPointContainerRepository
                .findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("EcoPointContainer not found: id=" + id)
                );
        boolean changesWasteType = entity.getWasteType() != request.getWasteType();
        if (changesWasteType && entity.getCurrentWeightGrams() > 0) {
            throw new IllegalStateException("Waste type cannot be changed until container is emptied");
        }
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

    @Transactional
    public EcoPointContainerResponseDto empty(Long id) {
        EcoPointContainer container = ecoPointContainerRepository
                        .findByIdForUpdate(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("EcoPointContainer not found: id=" + id)
                        );
        int previousFullness = container.getFullnessPercentage();
        container.empty();
        EcoPointContainer saved = ecoPointContainerRepository.save(container);
        EcoPointContainerResponseDto response = ecoPointContainerMapper.toDto(saved);
        eventPublisher.publishEvent(
                new EcoPointContainerChangedEvent(
                        response,
                        previousFullness,
                        LocalDateTime.now()
                )
        );
        return response;
    }

    private EcoPointContainer getOrThrow(Long id) {
        return ecoPointContainerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("EcoPointContainer not found: id=" + id));
    }
}