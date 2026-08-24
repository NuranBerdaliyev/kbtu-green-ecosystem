package com.example.green.domain.entity;

import com.example.green.domain.enums.TripPaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "trip_participants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_trip_passenger",
                        columnNames = {"trip_id", "passenger_id"}
                )
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @NotNull
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @NotNull
    @Column(name = "is_cancelled", nullable = false)
    private Boolean isCancelled;

    @NotNull
    @Min(0)
    @Column(name = "reserved_eco_coins", nullable = false)
    private Long reservedEcoCoins;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private TripPaymentStatus paymentStatus;

    public void rejoin(long fare) {
        if (!Boolean.TRUE.equals(isCancelled)
                || paymentStatus != TripPaymentStatus.REFUNDED) {
            throw new IllegalStateException(
                    "Only refunded participation can be rejoined"
            );
        }

        validateFare(fare);

        joinedAt = LocalDateTime.now();
        isCancelled = false;
        reservedEcoCoins = fare;
        paymentStatus = TripPaymentStatus.RESERVED;
    }

    public void refundAndCancel() {
        if (Boolean.TRUE.equals(isCancelled)
                || paymentStatus != TripPaymentStatus.RESERVED) {
            throw new IllegalStateException(
                    "Only active reserved payment can be refunded"
            );
        }

        isCancelled = true;
        paymentStatus = TripPaymentStatus.REFUNDED;
    }

    public void settle() {
        if (Boolean.TRUE.equals(isCancelled)
                || paymentStatus != TripPaymentStatus.RESERVED) {
            throw new IllegalStateException(
                    "Only active reserved payment can be settled"
            );
        }

        paymentStatus = TripPaymentStatus.SETTLED;
    }

    private void validateFare(long fare) {
        if (fare <= 0) {
            throw new IllegalArgumentException(
                    "Carpool fare must be greater than zero"
            );
        }
    }
}