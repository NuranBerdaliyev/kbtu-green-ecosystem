package com.example.green.api.dto.request;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripRequestDto {

    @NotNull
    private String departureLocationWkt;

    @NotNull
    private LocalDateTime departureTime;

    @NotNull
    @Min(1)
    private Integer totalSeats;
}
