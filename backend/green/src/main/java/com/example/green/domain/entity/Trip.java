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

    @NotNull(message = "Водитель обязателен")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @NotNull(message = "Точка отправления обязательна")
    @Column(name = "departure_location", nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point departureLocation;

    @NotNull(message = "Время отправления обязательно")
    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @NotNull(message = "Количество мест обязательно")
    @Min(value = 1, message = "totalSeats должно быть больше 0")
    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @NotNull(message = "Свободные места обязательны")
    @Min(value = 0, message = "availableSeats не может быть отрицательным")
    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @NotNull(message = "Статус поездки обязателен")
    @Enumerated(EnumType.STRING)
    @Column(name = "trip_status", nullable = false, length = 20)
    private TripStatus tripStatus;

    @Builder.Default
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripParticipant> tripParticipants = new ArrayList<>();

    @AssertTrue(message = "availableSeats не может быть больше totalSeats")
    public boolean isSeatsValid() {
        if (availableSeats == null || totalSeats == null) return true;
        return availableSeats <= totalSeats;
    }
}