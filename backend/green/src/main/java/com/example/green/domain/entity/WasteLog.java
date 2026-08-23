package com.example.green.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "waste_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WasteLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User обязателен")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "EcoPointContainer обязателен")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eco_point_container_id", nullable = false)
    private EcoPointContainer ecoPointContainer;

    @NotNull(message = "scannedAt обязателен")
    @Column(name = "scanned_at", nullable = false)
    private LocalDateTime scannedAt;

    @NotNull(message = "ecoCoinsEarned обязателен")
    @Min(value = 0, message = "ecoCoinsEarned не может быть отрицательным")
    @Column(name = "eco_coins_earned", nullable = false)
    private Integer ecoCoinsEarned;

    @NotNull
    @Min(0)
    @Column(name = "waste_weight_grams", nullable = false)
    private Integer wasteWeightGrams;
}
