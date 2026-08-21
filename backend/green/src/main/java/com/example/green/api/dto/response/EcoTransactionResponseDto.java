package com.example.green.api.dto.response;

import com.example.green.domain.enums.EcoTransactionSource;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EcoTransactionResponseDto {
    private Long id;
    private EcoTransactionSource source;
    private Long referenceId;
    private Long ecoCoinsDelta;
    private Integer esgRatingDelta;
    private BigDecimal co2SavedDelta;
    private LocalDateTime createdAt;
}