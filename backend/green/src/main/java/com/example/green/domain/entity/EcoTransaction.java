package com.example.green.domain.entity;

import com.example.green.domain.enums.EcoTransactionSource;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "eco_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EcoTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private EcoTransactionSource source;

    @NotNull
    @Column(name = "reference_id")
    private Long referenceId; // id поездки / депозита

    @NotNull
    @Column(name = "eco_coins_delta", nullable = false)
    private Long ecoCoinsDelta;

    @NotNull
    @Column(name = "esg_rating_delta", nullable = false)
    private Integer esgRatingDelta;

    @Column(name = "co2_saved_delta", precision = 15, scale = 3)
    private BigDecimal co2SavedDelta;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}