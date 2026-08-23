package com.example.green.api.dto.request;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripRequestDto {

    @NotBlank
    private String departureLocationWkt;

    @NotBlank
    private String destinationLocationWkt;

    @NotNull
    @Future(message = "Departure time must be in the future")
    private LocalDateTime departureTime;

    @NotNull
    @Min(1)
    private Integer totalSeats;
}
