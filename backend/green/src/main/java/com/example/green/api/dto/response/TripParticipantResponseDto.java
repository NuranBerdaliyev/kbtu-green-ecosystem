package com.example.green.api.dto.response;

import com.example.green.domain.enums.TripPaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripParticipantResponseDto {
    private Long id;
    private Long tripId;
    private Long passengerId;
    private LocalDateTime joinedAt;
    private Boolean isCancelled;
    private Long reservedEcoCoins;
    private TripPaymentStatus paymentStatus;
}
