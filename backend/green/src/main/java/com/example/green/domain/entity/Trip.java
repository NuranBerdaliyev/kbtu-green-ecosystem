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

    @NotNull(message = "Departure location cannot be blank")
    @Min(value = 1, message = "totalSeats должно быть больше 0")
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

    @Column(name = "destination_location", columnDefinition = "geometry(Point,4326)")
    private Point destinationLocation;

    @Builder.Default
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripParticipant> tripParticipants = new ArrayList<>();

    public boolean isTerminal() {
        return tripStatus == TripStatus.COMPLETED || tripStatus == TripStatus.CANCELLED;
    }

    public boolean isPublished() {
        return tripStatus == TripStatus.ACTIVE || isTerminal();
    }

    public void validateMutable() {
        if (isTerminal()) {
            throw new IllegalStateException("Trip is immutable in status: " + tripStatus);
        }
    }

    public void changeStatus(TripStatus next) {
        if (tripStatus == null || next == null) {
            throw new IllegalArgumentException("Trip status cannot be null");
        }

        boolean allowed = switch (tripStatus) {
            case CREATED -> next == TripStatus.ACTIVE || next == TripStatus.CANCELLED;
            case ACTIVE -> next == TripStatus.COMPLETED || next == TripStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };

        if (!allowed) {
            throw new IllegalStateException("Invalid trip status transition: " + tripStatus + " -> " + next);
        }

        this.tripStatus = next;
    }

    public void occupySeat() {
        validateMutable();
        if (availableSeats == null || availableSeats <= 0) {
            throw new IllegalStateException("No available seats");
        }
        this.availableSeats -= 1;
    }

    public void releaseSeat() {
        validateMutable();
        if (availableSeats == null || totalSeats == null || availableSeats >= totalSeats) {
            throw new IllegalStateException("Cannot release seat beyond totalSeats");
        }
        this.availableSeats += 1;
    }
    @AssertTrue(message = "availableSeats cannot be more than totalSeats")
    public boolean isSeatsValid() {
        if (availableSeats == null || totalSeats == null) return true;
        return availableSeats <= totalSeats;
    }
}