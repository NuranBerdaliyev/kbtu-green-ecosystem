package com.example.green.domain.repository;

import com.example.green.domain.entity.UserAchievement;
import com.example.green.domain.enums.AchievementCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUserIdOrderByUnlockedAtAsc(Long userId);
    boolean existsByUserIdAndCode(Long userId, AchievementCode code);
    long countByUserId(Long userId);
}