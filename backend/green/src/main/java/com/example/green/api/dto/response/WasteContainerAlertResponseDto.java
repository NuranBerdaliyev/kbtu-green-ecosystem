package com.example.green.api.dto.response;

import com.example.green.domain.enums.WasteType;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WasteContainerAlertResponseDto {
    private Long containerId;
    private String title;
    private WasteType wasteType;
    private Integer previousFullnessPercentage;
    private Integer currentFullnessPercentage;
    private Integer currentWeightGrams;
    private Integer capacityGrams;
    private LocalDateTime crossedAt;
}