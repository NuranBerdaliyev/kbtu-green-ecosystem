package com.example.green.api.mapper;

import com.example.green.api.dto.response.*;
import com.example.green.domain.entity.EcoTransaction;
import com.example.green.domain.entity.User;
import com.example.green.domain.entity.UserAchievement;
import com.example.green.domain.enums.AchievementCode;
import org.springframework.stereotype.Component;

@Component
public class GamificationMapper {

    public EcoTransactionResponseDto toEcoTransactionDto(EcoTransaction transaction) {
        return EcoTransactionResponseDto.builder()
                .id(transaction.getId())
                .source(transaction.getSource())
                .referenceId(transaction.getReferenceId())
                .ecoCoinsDelta(transaction.getEcoCoinsDelta())
                .esgRatingDelta(transaction.getEsgRatingDelta())
                .co2SavedDelta(transaction.getCo2SavedDelta())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    public AchievementResponseDto toAchievementDto(AchievementCode code, UserAchievement unlockedAchievement) {
        boolean unlocked = unlockedAchievement != null;

        return AchievementResponseDto.builder()
                .code(code)
                .title(code.getTitle())
                .description(code.getDescription())
                .unlocked(unlocked)
                .unlockedAt(
                        unlocked ? unlockedAchievement.getUnlockedAt() : null
                )
                .build();
    }

    public LeaderboardEntryResponseDto toLeaderboardDto(User user, long rank) {
        return LeaderboardEntryResponseDto.builder()
                .rank(rank)
                .userId(user.getId())
                .fullName(user.getFullName())
                .esgRating(user.getEsgRating())
                .ecoCoinsBalance(user.getEcoCoinsBalance())
                .totalCo2Saved(user.getTotalCo2Saved())
                .build();
    }

    public GamificationProfileResponseDto toGamificationProfileDto(User user, Long rank, long achievementCount) {
        return GamificationProfileResponseDto.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .ecoCoinsBalance(user.getEcoCoinsBalance())
                .esgRating(user.getEsgRating())
                .totalCo2Saved(user.getTotalCo2Saved())
                .leaderboardRank(rank)
                .unlockedAchievements(achievementCount)
                .build();
    }
}