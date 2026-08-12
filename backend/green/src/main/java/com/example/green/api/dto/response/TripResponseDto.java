package com.example.green.api.dto.response;

import com.example.green.domain.enums.TripStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripResponseDto {
    private Long id;
    private Long driverId;
    private String departureLocationWkt;
    private LocalDateTime departureTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private TripStatus tripStatus;
}
