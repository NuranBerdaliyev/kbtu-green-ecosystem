package com.example.green.service;

import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.domain.entity.EcoTransaction;
import com.example.green.domain.entity.User;
import com.example.green.domain.enums.EcoTransactionSource;
import com.example.green.domain.repository.EcoTransactionRepository;
import com.example.green.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GamificationService {
    private final UserRepository userRepository;
    private final EcoTransactionRepository ecoTransactionRepository;

    @Transactional
    public int processDepositRewards(Long userId, Long containerId, Integer wasteWeightGrams) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        // 积分与ESG计算规则 (示例：每100g = 1 EcoCoin, 每次成功投递 +1 ESG)
        int ecoCoinsEarned = Math.max(1, wasteWeightGrams / 100);
        int esgIncrease = 1;
        BigDecimal co2Saved = BigDecimal.valueOf(wasteWeightGrams * 0.005); // 碳减排计算系数

        // 更新用户信息
        user.setEcoCoinsBalance(user.getEcoCoinsBalance() + ecoCoinsEarned);
        user.setEsgRating(Math.min(100, user.getEsgRating() + esgIncrease)); // ESG 封顶100
        user.setTotalCo2Saved(user.getTotalCo2Saved().add(co2Saved));
        userRepository.save(user);

        // 写入交易记录
        EcoTransaction tx = EcoTransaction.builder()
                .user(user)
                .source(EcoTransactionSource.WASTE_DEPOSIT)
                .referenceId(containerId)
                .ecoCoinsDelta((long) ecoCoinsEarned)
                .co2SavedDelta(co2Saved)
                .createdAt(LocalDateTime.now())
                .build();
        ecoTransactionRepository.save(tx);

        return ecoCoinsEarned;
    }
}