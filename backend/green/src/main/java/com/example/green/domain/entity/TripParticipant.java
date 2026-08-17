package com.example.green.domain.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trip_participants", uniqueConstraints = {
        @UniqueConstraint(name = "uk_trip_passenger", columnNames = {"trip_id", "passenger_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Trip cannot be blank")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @NotNull(message = "Passenger cannot be blank")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @NotNull(message = "joinedAt cannot be blank")
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @NotNull(message = "isCancelled cannot be blank")
    @Column(name = "is_cancelled", nullable = false)
    private Boolean isCancelled;
}
