package com.example.green.service;

import com.example.green.api.dto.response.GamificationProfileResponseDto;
import com.example.green.domain.entity.User;
import com.example.green.domain.enums.AchievementCode;
import com.example.green.domain.enums.Role;
import com.example.green.domain.enums.WasteType;
import com.example.green.domain.model.RewardResult;
import com.example.green.domain.repository.EcoTransactionRepository;
import com.example.green.domain.repository.UserAchievementRepository;
import com.example.green.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
class GamificationIntegrationTest {

    private static final DockerImageName POSTGIS_IMAGE =
            DockerImageName.parse("postgis/postgis:16-3.4")
                    .asCompatibleSubstituteFor("postgres");

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(POSTGIS_IMAGE)
                    .withDatabaseName("green_test")
                    .withUsername("green_test")
                    .withPassword("green_test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );
        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );
        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "validate"
        );
        registry.add(
                "spring.flyway.enabled",
                () -> true
        );
    }

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EcoTransactionRepository ecoTransactionRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @MockBean
    private CurrentUserService currentUserService;

    @BeforeEach
    void cleanDatabase() {
        userAchievementRepository.deleteAllInBatch();
        ecoTransactionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void completedTripShouldUpdateUserAndCreateTransaction() {
        User user = userRepository.save(user(
                "student@test.com",
                Role.STUDENT,
                0,
                0L,
                "0.000"
        ));

        RewardResult result =
                gamificationService.rewardForCompletedTrip(
                        user.getId(),
                        100L,
                        10.0
                );

        User updated = userRepository.findById(user.getId())
                .orElseThrow();

        assertTrue(result.applied());
        assertEquals(20L, result.ecoCoinsEarned());
        assertEquals(2, result.esgRatingEarned());
        assertEquals(
                new BigDecimal("1.200"),
                result.co2Saved()
        );

        assertEquals(20L, updated.getEcoCoinsBalance());
        assertEquals(2, updated.getEsgRating());
        assertEquals(
                new BigDecimal("1.200"),
                updated.getTotalCo2Saved()
        );

        assertEquals(1, ecoTransactionRepository.count());

        assertTrue(
                userAchievementRepository.existsByUserIdAndCode(
                        user.getId(),
                        AchievementCode.FIRST_ACTION
                )
        );

        assertTrue(
                userAchievementRepository.existsByUserIdAndCode(
                        user.getId(),
                        AchievementCode.FIRST_SHARED_TRIP
                )
        );
    }

    @Test
    void sameTripShouldNotGiveRewardTwice() {
        User user = userRepository.save(user(
                "student@test.com",
                Role.STUDENT,
                0,
                0L,
                "0.000"
        ));

        RewardResult first =
                gamificationService.rewardForCompletedTrip(
                        user.getId(),
                        100L,
                        10.0
                );

        RewardResult second =
                gamificationService.rewardForCompletedTrip(
                        user.getId(),
                        100L,
                        10.0
                );

        User updated = userRepository.findById(user.getId())
                .orElseThrow();

        assertTrue(first.applied());
        assertFalse(second.applied());

        assertEquals(20L, updated.getEcoCoinsBalance());
        assertEquals(2, updated.getEsgRating());
        assertEquals(
                new BigDecimal("1.200"),
                updated.getTotalCo2Saved()
        );

        assertEquals(1, ecoTransactionRepository.count());
    }

    @Test
    void wasteDepositShouldUpdateGamificationData() {
        User user = userRepository.save(user(
                "student@test.com",
                Role.STUDENT,
                0,
                0L,
                "0.000"
        ));

        RewardResult result =
                gamificationService.rewardForWasteDeposit(
                        user.getId(),
                        200L,
                        250,
                        WasteType.PLASTIC
                );

        User updated = userRepository.findById(user.getId())
                .orElseThrow();

        assertTrue(result.applied());
        assertEquals(2L, result.ecoCoinsEarned());
        assertEquals(1, result.esgRatingEarned());
        assertEquals(
                new BigDecimal("0.375"),
                result.co2Saved()
        );

        assertEquals(2L, updated.getEcoCoinsBalance());
        assertEquals(1, updated.getEsgRating());
        assertEquals(
                new BigDecimal("0.375"),
                updated.getTotalCo2Saved()
        );

        assertTrue(
                userAchievementRepository.existsByUserIdAndCode(
                        user.getId(),
                        AchievementCode.FIRST_WASTE_DEPOSIT
                )
        );
    }

    @Test
    void esgRatingShouldNotExceedOneHundred() {
        User user = userRepository.save(user(
                "student@test.com",
                Role.STUDENT,
                99,
                0L,
                "0.000"
        ));

        RewardResult result =
                gamificationService.rewardForCompletedTrip(
                        user.getId(),
                        100L,
                        5.0
                );

        User updated = userRepository.findById(user.getId())
                .orElseThrow();

        assertEquals(100, updated.getEsgRating());
        assertEquals(1, result.esgRatingEarned());
    }

    @Test
    void profileShouldCalculateCorrectLeaderboardRank() {
        userRepository.save(user(
                "leader@test.com",
                Role.STUDENT,
                90,
                100L,
                "5.000"
        ));

        User currentUser = userRepository.save(user(
                "current@test.com",
                Role.STUDENT,
                70,
                50L,
                "2.000"
        ));

        userRepository.save(user(
                "lower@test.com",
                Role.EMPLOYEE,
                40,
                500L,
                "20.000"
        ));

        // HR не должен участвовать в лидерборде
        userRepository.save(user(
                "hr@test.com",
                Role.HR,
                100,
                1000L,
                "100.000"
        ));

        when(currentUserService.getCurrentUserOrThrow())
                .thenReturn(currentUser);

        GamificationProfileResponseDto profile =
                gamificationService.getMyProfile();

        assertEquals(2L, profile.getLeaderboardRank());
    }

    @Test
    void leaderboardShouldSortAndExcludeHr() {
        User second = userRepository.save(user(
                "second@test.com",
                Role.EMPLOYEE,
                70,
                100L,
                "5.000"
        ));

        User first = userRepository.save(user(
                "first@test.com",
                Role.STUDENT,
                90,
                50L,
                "2.000"
        ));

        userRepository.save(user(
                "hr@test.com",
                Role.HR,
                100,
                1000L,
                "100.000"
        ));

        var leaderboard =
                gamificationService.getLeaderboard(0, 20);

        assertEquals(2, leaderboard.getTotalElements());

        assertEquals(
                first.getId(),
                leaderboard.getContent().get(0).getUserId()
        );

        assertEquals(
                second.getId(),
                leaderboard.getContent().get(1).getUserId()
        );

        assertEquals(
                1L,
                leaderboard.getContent().get(0).getRank()
        );

        assertEquals(
                2L,
                leaderboard.getContent().get(1).getRank()
        );
    }

    private User user(
            String email,
            Role role,
            int esgRating,
            long ecoCoins,
            String co2
    ) {
        return User.builder()
                .email(email)
                .fullName("Test User")
                .role(role)
                .passwordHash("x".repeat(60))
                .ecoCoinsBalance(ecoCoins)
                .esgRating(esgRating)
                .totalCo2Saved(new BigDecimal(co2))
                .createdAt(LocalDateTime.now())
                .build();
    }
}