package com.example.green.api.controller;

import com.example.green.api.dto.response.*;
import com.example.green.service.GamificationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
@Validated
public class GamificationController {

    private final GamificationService gamificationService;

    @GetMapping("/me")
    public GamificationProfileResponseDto myProfile() {
        return gamificationService.getMyProfile();
    }

    @GetMapping("/me/history")
    public Page<EcoTransactionResponseDto> myHistory(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return gamificationService.getMyHistory(page, size);
    }

    @GetMapping("/me/achievements")
    public List<AchievementResponseDto> myAchievements() {
        return gamificationService.getMyAchievements();
    }

    @GetMapping("/leaderboard")
    public Page<LeaderboardEntryResponseDto> leaderboard(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return gamificationService.getLeaderboard(
                page,
                size
        );
    }
}