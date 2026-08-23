package com.example.green.api.dto.response;

import com.example.green.domain.enums.WasteType;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EcoPointContainerResponseDto {
    private Long id;
    private String title;
    private String locationWkt;
    private WasteType wasteType;
    private Integer fullnessPercentage;
    private Integer capacityGrams;
    private Integer currentWeightGrams;
    private Boolean isActive;
    private String qrCodeToken;
}
