package com.example.green.service.event;

import com.example.green.api.dto.response.EcoPointContainerResponseDto;

import java.time.LocalDateTime;

public record EcoPointContainerChangedEvent(
        EcoPointContainerResponseDto container,
        Integer previousFullnessPercentage,
        LocalDateTime occurredAt
) {}