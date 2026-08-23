package com.example.green.service;

import com.example.green.api.dto.response.*;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.GamificationMapper;
import com.example.green.config.GamificationProperties;
import com.example.green.domain.entity.*;
import com.example.green.domain.enums.*;
import com.example.green.domain.model.RewardResult;
import com.example.green.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class GamificationService {
    private static final List<Role> RANKED_ROLES = List.of(Role.STUDENT, Role.EMPLOYEE);
    private final UserRepository userRepository;
    private final EcoTransactionRepository ecoTransactionRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final CurrentUserService currentUserService;
    private final GamificationMapper gamificationMapper;
    private final GamificationProperties properties;

    @Transactional
    public RewardResult rewardForCompletedTrip(Long userId, Long tripId, double distanceKm) {
        if (distanceKm <= 0) {
            throw new IllegalArgumentException("Trip distance must be greater than zero");
        }

        BigDecimal distance = BigDecimal.valueOf(distanceKm);
        long coins = distance
                .multiply(properties.getTrip().getCoinsPerKm())
                .setScale(0, RoundingMode.HALF_UP)
                .max(BigDecimal.ONE)
                .longValueExact();

        BigDecimal co2Saved = distance.multiply(properties.getTrip().getCo2KgPerPassengerKm()).setScale(3, RoundingMode.HALF_UP);
        return applyReward(
                userId,
                EcoTransactionSource.TRIP_COMPLETED,
                tripId,
                coins,
                properties.getTrip().getEsgPerCompletedTrip(),
                co2Saved
        );
    }

    @Transactional
    public RewardResult rewardForWasteDeposit(Long userId, Long wasteLogId, int wasteWeightGrams, WasteType wasteType) {
        if (wasteWeightGrams <= 0) {
            throw new IllegalArgumentException("Waste weight must be greater than zero");
        }
        int gramsPerCoin = properties.getWaste().getGramsPerCoin();
        long coins = Math.max(1, wasteWeightGrams / gramsPerCoin);
        BigDecimal weightKg = BigDecimal.valueOf(wasteWeightGrams)
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
        BigDecimal co2Saved = weightKg
                .multiply(properties.getWaste().co2Coefficient(wasteType))
                .setScale(3, RoundingMode.HALF_UP);

        return applyReward(
                userId,
                EcoTransactionSource.WASTE_DEPOSIT,
                wasteLogId,
                coins,
                properties.getWaste().getEsgPerDeposit(),
                co2Saved
        );
    }

    @Transactional(readOnly = true)
    public boolean isRecommended(User user) {
        return user.getEsgRating() != null
                && user.getEsgRating()
                >= properties.getRecommendedEsgThreshold();
    }

    @Transactional(readOnly = true)
    public GamificationProfileResponseDto getMyProfile() {
        User user = currentUserService.getCurrentUserOrThrow();
        Long rank = isRankedRole(user.getRole()) ? calculateRank(user) : null;
        long achievementCount = userAchievementRepository.countByUserId(user.getId());
        return gamificationMapper.toGamificationProfileDto(user, rank, achievementCount);
    }

    @Transactional(readOnly = true)
    public Page<EcoTransactionResponseDto> getMyHistory(int page, int size) {
        User user = currentUserService.getCurrentUserOrThrow();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return ecoTransactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(gamificationMapper::toEcoTransactionDto);
    }

    @Transactional(readOnly = true)
    public List<AchievementResponseDto> getMyAchievements() {
        User user = currentUserService.getCurrentUserOrThrow();

        Map<AchievementCode, UserAchievement> unlocked = userAchievementRepository
                        .findByUserIdOrderByUnlockedAtAsc(user.getId())
                        .stream()
                        .collect(Collectors.toMap(UserAchievement::getCode, Function.identity()));

        return Arrays.stream(AchievementCode.values())
                .map(code -> gamificationMapper.toAchievementDto(code, unlocked.get(code)))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<LeaderboardEntryResponseDto> getLeaderboard(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(
                        Sort.Order.desc("esgRating"), Sort.Order.desc("ecoCoinsBalance"),
                        Sort.Order.desc("totalCo2Saved"), Sort.Order.asc("id")
                )
        );

        Page<User> users = userRepository.findByRoleIn(RANKED_ROLES, pageable);
        long firstRank = pageable.getOffset() + 1;
        List<LeaderboardEntryResponseDto> content = IntStream.range(0, users.getContent().size())
                .mapToObj(index ->
                                gamificationMapper.toLeaderboardDto(
                                        users.getContent().get(index),
                                        firstRank + index
                                )
                        )
                        .toList();

        return new PageImpl<>(content, pageable, users.getTotalElements());
    }

    private RewardResult applyReward(
            Long userId,
            EcoTransactionSource source,
            Long referenceId,
            long ecoCoinsDelta,
            int requestedEsgDelta,
            BigDecimal co2Delta
    ) {
        if (referenceId == null || referenceId <= 0) {
            throw new IllegalArgumentException("Reward referenceId must be positive");
        }

        User user = userRepository
                .findByIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + userId));

        Optional<EcoTransaction> existing =
                ecoTransactionRepository
                        .findByUserIdAndSourceAndReferenceId(
                                userId,
                                source,
                                referenceId
                        );

        if (existing.isPresent()) {
            EcoTransaction transaction = existing.get();

            return new RewardResult(
                    transaction.getEcoCoinsDelta(),
                    transaction.getEsgRatingDelta(),
                    zeroIfNull(transaction.getCo2SavedDelta()),
                    false,
                    List.of()
            );
        }

        long newBalance = Math.addExact(user.getEcoCoinsBalance(), ecoCoinsDelta);

        int oldEsg = user.getEsgRating();
        int newEsg = Math.min(100, oldEsg + requestedEsgDelta);
        int actualEsgDelta = newEsg - oldEsg;

        BigDecimal normalizedCo2 = zeroIfNull(co2Delta)
                        .setScale(
                                3,
                                RoundingMode.HALF_UP
                        );

        user.setEcoCoinsBalance(newBalance);
        user.setEsgRating(newEsg);
        user.setTotalCo2Saved(
                user.getTotalCo2Saved()
                        .add(normalizedCo2)
        );

        userRepository.save(user);

        EcoTransaction transaction =
                EcoTransaction.builder()
                        .user(user)
                        .source(source)
                        .referenceId(referenceId)
                        .ecoCoinsDelta(ecoCoinsDelta)
                        .esgRatingDelta(actualEsgDelta)
                        .co2SavedDelta(normalizedCo2)
                        .createdAt(LocalDateTime.now())
                        .build();

        ecoTransactionRepository.saveAndFlush(transaction);

        List<AchievementCode> unlocked = evaluateAchievements(user);

        return new RewardResult(
                ecoCoinsDelta,
                actualEsgDelta,
                normalizedCo2,
                true,
                unlocked
        );
    }

    private List<AchievementCode> evaluateAchievements(User user) {
        long totalActions = ecoTransactionRepository.countByUserId(user.getId());

        long completedTrips = ecoTransactionRepository.countByUserIdAndSource(user.getId(), EcoTransactionSource.TRIP_COMPLETED);
        long wasteDeposits = ecoTransactionRepository.countByUserIdAndSource(user.getId(), EcoTransactionSource.WASTE_DEPOSIT);

        Set<AchievementCode> existingCodes =
                userAchievementRepository
                        .findByUserIdOrderByUnlockedAtAsc(
                                user.getId()
                        )
                        .stream()
                        .map(UserAchievement::getCode)
                        .collect(Collectors.toSet());

        List<UserAchievement> newEntities = new ArrayList<>();
        unlockIfEligible(
                totalActions >= 1,
                AchievementCode.FIRST_ACTION,
                user,
                existingCodes,
                newEntities
        );

        unlockIfEligible(
                completedTrips >= 1,
                AchievementCode.FIRST_SHARED_TRIP,
                user,
                existingCodes,
                newEntities
        );

        unlockIfEligible(
                completedTrips >= 10,
                AchievementCode.CARPOOL_REGULAR,
                user,
                existingCodes,
                newEntities
        );

        unlockIfEligible(
                wasteDeposits >= 1,
                AchievementCode.FIRST_WASTE_DEPOSIT,
                user,
                existingCodes,
                newEntities
        );

        unlockIfEligible(
                wasteDeposits >= 10,
                AchievementCode.RECYCLING_REGULAR,
                user,
                existingCodes,
                newEntities
        );

        unlockIfEligible(
                user.getEcoCoinsBalance() >= 100,
                AchievementCode.ECOCOINS_100,
                user,
                existingCodes,
                newEntities
        );

        unlockIfEligible(
                user.getEsgRating() >= 70,
                AchievementCode.ESG_70,
                user,
                existingCodes,
                newEntities
        );

        unlockIfEligible(
                user.getTotalCo2Saved()
                        .compareTo(BigDecimal.TEN) >= 0,
                AchievementCode.CO2_10_KG,
                user,
                existingCodes,
                newEntities
        );

        userAchievementRepository.saveAll(newEntities);

        return newEntities.stream()
                .map(UserAchievement::getCode)
                .toList();
    }

    private void unlockIfEligible(
            boolean eligible,
            AchievementCode code,
            User user,
            Set<AchievementCode> existingCodes,
            List<UserAchievement> newEntities
    ) {
        if (!eligible || !existingCodes.add(code)) {
            return;
        }

        newEntities.add(
                UserAchievement.builder()
                        .user(user)
                        .code(code)
                        .unlockedAt(LocalDateTime.now())
                        .build()
        );
    }

    private Long calculateRank(User user) {
        return userRepository.calculateRank(
                RANKED_ROLES,
                user.getId(),
                user.getEsgRating(),
                user.getEcoCoinsBalance(),
                user.getTotalCo2Saved()
        );
    }

    private boolean isRankedRole(Role role) {
        return RANKED_ROLES.contains(role);
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }
}