package com.example.green.api.dto.response;

import com.example.green.domain.enums.AchievementCode;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AchievementResponseDto {

    private AchievementCode code;
    private String title;
    private String description;
    private Boolean unlocked;
    private LocalDateTime unlockedAt;
}