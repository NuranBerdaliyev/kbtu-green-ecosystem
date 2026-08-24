package com.example.green.domain.entity;

import com.example.green.domain.enums.TripStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trips", indexes = {
        @Index(name = "idx_trips_status_time", columnList = "trip_status,departure_time")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Driver cannot be blank")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @NotNull(message = "Departure location cannot be blank")
    @Column(name = "departure_location", nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point departureLocation;

    @NotNull(message = "Departure time cannot be blank")
    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @NotNull
    @Min(value = 1, message = "Total seats must be at least 1")
    @Max(value = 8, message = "Total seats cannot exceed 8")
    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @NotNull(message = "Available seats cannot be blank")
    @Min(value = 0, message = "availableSeats не может быть отрицательным")
    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @NotNull(message = "Trip status cannot be blank")
    @Enumerated(EnumType.STRING)

    @Column(name = "trip_status", nullable = false, length = 20)
    private TripStatus tripStatus;

    @NotNull(message = "Destination location cannot be blank")
    @Column(name = "destination_location", nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point destinationLocation;

    @Builder.Default
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripParticipant> tripParticipants = new ArrayList<>();

    @NotNull
    @Min(1)
    @Max(100_000)
    @Column(name = "price_eco_coins", nullable = false)
    private Long priceEcoCoins;

    public boolean isTerminal() {
        return tripStatus == TripStatus.COMPLETED || tripStatus == TripStatus.CANCELLED;
    }

    public void changeStatus(TripStatus next) {
        if (tripStatus == null || next == null) {
            throw new IllegalArgumentException("Trip status cannot be null");
        }

        boolean allowed = switch (tripStatus) {
            case CREATED ->
                    next == TripStatus.PUBLISHED
                            || next == TripStatus.CANCELLED;

            case PUBLISHED ->
                    next == TripStatus.IN_PROGRESS
                            || next == TripStatus.CANCELLED;

            case IN_PROGRESS ->
                    next == TripStatus.COMPLETED
                            || next == TripStatus.CANCELLED;

            case COMPLETED, CANCELLED -> false;
        };

        if (!allowed) {
            throw new IllegalStateException(
                    "Invalid trip status transition: "
                            + tripStatus
                            + " -> "
                            + next
            );
        }

        tripStatus = next;
    }

    public void occupySeat() {
        if (tripStatus != TripStatus.PUBLISHED) {
            throw new IllegalStateException("Seats can only be occupied in PUBLISHED trips");
        }

        if (availableSeats == null || availableSeats <= 0) {
            throw new IllegalStateException("No available seats");
        }

        availableSeats -= 1;
    }

    public void releaseSeat() {
        if (tripStatus != TripStatus.PUBLISHED) {
            throw new IllegalStateException("Seats can only be released in PUBLISHED trips");
        }

        if (availableSeats == null
                || totalSeats == null
                || availableSeats >= totalSeats) {
            throw new IllegalStateException("Cannot release seat beyond total seats");
        }

        availableSeats += 1;
    }
}