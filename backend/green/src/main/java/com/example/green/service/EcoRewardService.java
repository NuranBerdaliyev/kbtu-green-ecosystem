package com.example.green.service;

import com.example.green.domain.entity.EcoTransaction;
import com.example.green.domain.entity.User;
import com.example.green.domain.enums.EcoTransactionSource;
import com.example.green.domain.repository.EcoTransactionRepository;
import com.example.green.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EcoRewardService {

    private static final BigDecimal CO2_KG_PER_KM = BigDecimal.valueOf(0.12); // экономия на 1 пассажира на 1 км
    private static final long COINS_PER_KM = 2;

    private final UserRepository userRepository;
    private final EcoTransactionRepository ecoTransactionRepository;

    public void rewardForTripDistance(User user, double distanceKm, Long tripId) {
        if (distanceKm <= 0) return;

        BigDecimal co2Saved = CO2_KG_PER_KM
                .multiply(BigDecimal.valueOf(distanceKm))
                .setScale(3, RoundingMode.HALF_UP);
        long coins = Math.round(distanceKm * COINS_PER_KM);

        user.setEcoCoinsBalance(user.getEcoCoinsBalance() + coins);
        user.setTotalCo2Saved(user.getTotalCo2Saved().add(co2Saved));
        userRepository.save(user);

        ecoTransactionRepository.save(EcoTransaction.builder()
                .user(user)
                .source(EcoTransactionSource.TRIP_COMPLETED)
                .referenceId(tripId)
                .ecoCoinsDelta(coins)
                .co2SavedDelta(co2Saved)
                .createdAt(LocalDateTime.now())
                .build());
    }
}