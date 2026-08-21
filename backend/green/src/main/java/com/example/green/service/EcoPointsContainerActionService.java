package com.example.green.service;

import com.example.green.api.dto.request.WasteDepositRequestDto;
import com.example.green.api.dto.response.EcoPointContainerResponseDto;
import com.example.green.api.dto.response.WasteLogResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.EcoPointContainerMapper;
import com.example.green.api.mapper.WasteLogMapper;
import com.example.green.domain.entity.*;
import com.example.green.domain.model.RewardResult;
import com.example.green.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EcoPointsContainerActionService {

    private final EcoPointContainerRepository containerRepository;
    private final WasteLogRepository wasteLogRepository;
    private final CurrentUserService currentUserService;
    private final GamificationService gamificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final EcoPointContainerMapper containerMapper;
    private final WasteLogMapper wasteLogMapper;

    @Transactional(readOnly = true)
    public List<EcoPointContainerResponseDto> getActiveContainers() {
        return containerRepository
                .findByIsActiveTrue()
                .stream()
                .map(containerMapper::toDto)
                .toList();
    }

    @Transactional
    public WasteLogResponseDto processDeposit(WasteDepositRequestDto request) {
        User user = currentUserService.getCurrentUserOrThrow();

        EcoPointContainer container = containerRepository
                        .findByQrCodeToken(
                                request.getQrCodeToken()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Invalid QR code or container not found")
                        );

        if (!Boolean.TRUE.equals(container.getIsActive())) {
            throw new IllegalStateException("Container is not active");
        }

        int newFullness = Math.min(
                100,
                container.getFullnessPercentage()
                        + request.getAddedFullnessPercentage()
        );

        container.setFullnessPercentage(newFullness);
        containerRepository.save(container);

        /*
         * Сначала создаём WasteLog, чтобы получить уникальный
         * id конкретной сдачи отходов.
         */
        WasteLog log = WasteLog.builder()
                .user(user)
                .ecoPointContainer(container)
                .scannedAt(LocalDateTime.now())
                .ecoCoinsEarned(0)
                .build();

        log = wasteLogRepository.save(log);

        RewardResult reward =
                gamificationService.rewardForWasteDeposit(
                        user.getId(),
                        log.getId(),
                        request.getWasteWeightGrams(),
                        container.getWasteType()
                );

        log.setEcoCoinsEarned(
                Math.toIntExact(reward.ecoCoinsEarned())
        );

        WasteLog savedLog = wasteLogRepository.save(log);

        EcoPointContainerResponseDto containerDto = containerMapper.toDto(container);

        messagingTemplate.convertAndSend(
                "/topic/eco-containers",
                containerDto
        );

        if (newFullness >= 90) {
            messagingTemplate.convertAndSend(
                    "/topic/admin/alerts",
                    "Container ID " + container.getId()
                            + " is " + newFullness
                            + "% full"
            );
        }

        return wasteLogMapper.toDto(savedLog);
    }
}