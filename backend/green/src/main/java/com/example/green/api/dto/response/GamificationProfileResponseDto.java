package com.example.green.api.dto.response;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GamificationProfileResponseDto {
    private Long userId;
    private String fullName;
    private Long ecoCoinsBalance;
    private Integer esgRating;
    private BigDecimal totalCo2Saved;
    private Long leaderboardRank;
    private Long unlockedAchievements;
}