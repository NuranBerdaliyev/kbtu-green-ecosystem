package com.example.green.api.dto.response;

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
}