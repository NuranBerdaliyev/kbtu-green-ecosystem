package com.example.green.api.dto.request;

import com.example.green.domain.enums.TripStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripRequestDto {
    @NotNull
    private Long driverId;

    @NotNull
    private String departureLocationWkt;

    @NotNull
    private LocalDateTime departureTime;

    @NotNull
    @Min(1)
    private Integer totalSeats;

    @NotNull
    @Min(0)
    private Integer availableSeats;

    private TripStatus tripStatus;
}
