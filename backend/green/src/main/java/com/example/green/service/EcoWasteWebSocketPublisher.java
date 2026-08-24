package com.example.green.service;

import com.example.green.api.dto.response.EcoPointContainerResponseDto;
import com.example.green.api.dto.response.WasteContainerAlertResponseDto;
import com.example.green.config.GamificationProperties;
import com.example.green.service.event.EcoPointContainerChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EcoWasteWebSocketPublisher {

    private static final String CONTAINERS_TOPIC = "/topic/eco-containers";
    private static final String ADMIN_ALERTS_TOPIC = "/topic/admin/alerts";

    private final SimpMessagingTemplate messagingTemplate;
    private final GamificationProperties properties;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleContainerChanged(EcoPointContainerChangedEvent event) {
        EcoPointContainerResponseDto container = event.container();

        messagingTemplate.convertAndSend(CONTAINERS_TOPIC, container);

        int threshold = properties
                .getWaste()
                .getAlertThresholdPercentage();

        int previous = event.previousFullnessPercentage();
        int current = container.getFullnessPercentage();

        boolean crossedThreshold = previous < threshold && current >= threshold;

        if (!crossedThreshold) {
            return;
        }

        WasteContainerAlertResponseDto alert =
                WasteContainerAlertResponseDto.builder()
                        .containerId(container.getId())
                        .title(container.getTitle())
                        .wasteType(container.getWasteType())
                        .previousFullnessPercentage(previous)
                        .currentFullnessPercentage(current)
                        .currentWeightGrams(container.getCurrentWeightGrams())
                        .capacityGrams(container.getCapacityGrams())
                        .crossedAt(event.occurredAt())
                        .build();

        messagingTemplate.convertAndSend(ADMIN_ALERTS_TOPIC, alert);
    }
}