package com.example.green.api.dto.response;

import com.example.green.domain.enums.WasteDepositStatus;
import com.example.green.domain.enums.WasteType;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WasteLogResponseDto {
    private Long id;
    private Long userId;
    private Long ecoPointContainerId;
    private LocalDateTime scannedAt;
    private Integer ecoCoinsEarned;
    private Integer wasteWeightGrams;
    private WasteType wasteType;
    private Integer fullnessDeltaPercentage;
    private WasteDepositStatus status;
    private Long reviewedById;
    private LocalDateTime reviewedAt;
}