package com.example.green.domain.entity;

import com.example.green.domain.enums.WasteDepositStatus;
import com.example.green.domain.enums.WasteType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "waste_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WasteLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eco_point_container_id", nullable = false)
    private EcoPointContainer ecoPointContainer;

    @NotNull
    @Column(name = "scanned_at", nullable = false)
    private LocalDateTime scannedAt;

    @NotNull
    @Min(0)
    @Column(name = "eco_coins_earned", nullable = false)
    private Integer ecoCoinsEarned;

    @NotNull
    @Min(0)
    @Column(name = "waste_weight_grams", nullable = false)
    private Integer wasteWeightGrams;

    /*
     * Исторический снимок типа контейнера.
     * Не следует позднее брать тип из изменяемого контейнера.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "waste_type", nullable = false, length = 20)
    private WasteType wasteType;

    /*
     * Для PENDING и REJECTED равен нулю.
     * Заполняется только при APPROVED.
     */
    @NotNull
    @Min(0)
    @Max(100)
    @Column(name = "fullness_delta_percentage", nullable = false)
    private Integer fullnessDeltaPercentage;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WasteDepositStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public void approve(User admin, int fullnessDeltaPercentage,
                        int ecoCoinsEarned, LocalDateTime reviewedAt) {
        requirePending();

        if (fullnessDeltaPercentage < 0 || fullnessDeltaPercentage > 100) {
            throw new IllegalArgumentException("Fullness delta must be between 0 and 100");
        }

        if (ecoCoinsEarned < 0) {
            throw new IllegalArgumentException("Earned EcoCoins cannot be negative");
        }

        this.status = WasteDepositStatus.APPROVED;
        this.fullnessDeltaPercentage = fullnessDeltaPercentage;
        this.ecoCoinsEarned = ecoCoinsEarned;
        this.reviewedBy = admin;
        this.reviewedAt = reviewedAt;
    }

    public void reject(User admin, LocalDateTime reviewedAt) {
        requirePending();

        this.status = WasteDepositStatus.REJECTED;
        this.fullnessDeltaPercentage = 0;
        this.ecoCoinsEarned = 0;
        this.reviewedBy = admin;
        this.reviewedAt = reviewedAt;
    }

    private void requirePending() {
        if (status != WasteDepositStatus.PENDING) {
            throw new IllegalStateException("Only PENDING waste deposit can be reviewed");
        }
    }
}