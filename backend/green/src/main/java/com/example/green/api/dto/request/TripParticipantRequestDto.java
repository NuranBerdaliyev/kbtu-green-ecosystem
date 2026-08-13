package com.example.green.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripParticipantRequestDto {
    @NotNull
    private Long tripId;

    @NotNull
    private Long passengerId;

    @NotNull
    private LocalDateTime joinedAt;

    @NotNull
    private Boolean isCancelled;
}
