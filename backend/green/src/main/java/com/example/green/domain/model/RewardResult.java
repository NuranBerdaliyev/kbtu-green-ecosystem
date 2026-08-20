package com.example.green.domain.model;

import com.example.green.domain.enums.AchievementCode;

import java.math.BigDecimal;
import java.util.List;

public record RewardResult(
        long ecoCoinsEarned,
        int esgRatingEarned,
        BigDecimal co2Saved,
        boolean applied,
        List<AchievementCode> newAchievements
) {
    public RewardResult {
        newAchievements = List.copyOf(newAchievements);
    }
}