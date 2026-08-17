package com.example.green.api.dto.request;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripSearchRequestDto {
    private LocalDateTime fromTime;
    private LocalDateTime toTime;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double originLat;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double originLng;

    @DecimalMin("0.0")
    private Double radiusKm;

    @Min(1)
    private Integer minSeats;

    @Min(0)
    @Builder.Default
    private Integer page = 0;

    @Min(1)
    @Builder.Default
    private Integer size = 20;

    @Builder.Default
    private String sort = "departureTime,asc";
}
