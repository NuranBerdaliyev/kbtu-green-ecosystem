package com.example.green.api.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaderboardEntryResponseDto {
    private Long rank;
    private Long userId;
    private String fullName;
    private Integer esgRating;
    private Long ecoCoinsBalance;
    private BigDecimal totalCo2Saved;
}