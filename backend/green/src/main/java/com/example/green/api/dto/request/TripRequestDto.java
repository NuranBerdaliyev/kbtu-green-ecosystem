package com.example.green.api.dto.request;
import jakarta.validation.constraints.*;
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
    @Max(8)
    private Integer totalSeats;

    @NotNull
    @Min(1)
    @Max(100_000)
    private Long priceEcoCoins;
}
