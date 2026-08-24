package com.example.green.api.mapper;

import com.example.green.api.dto.response.TripParticipantResponseDto;
import com.example.green.domain.entity.Trip;
import com.example.green.domain.entity.TripParticipant;
import com.example.green.domain.entity.User;
import com.example.green.domain.enums.TripPaymentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TripParticipantMapper {
    public TripParticipant toEntity(Trip trip, User passenger) {
        return TripParticipant.builder()
                .trip(trip)
                .passenger(passenger)
                .joinedAt(LocalDateTime.now())
                .isCancelled(false)
                .reservedEcoCoins(trip.getPriceEcoCoins())
                .paymentStatus(TripPaymentStatus.RESERVED)
                .build();
    }

    public TripParticipantResponseDto toDto(TripParticipant entity) {
        return TripParticipantResponseDto.builder()
                .id(entity.getId())
                .tripId(entity.getTrip().getId())
                .passengerId(entity.getPassenger().getId())
                .joinedAt(entity.getJoinedAt())
                .isCancelled(entity.getIsCancelled())
                .reservedEcoCoins(entity.getReservedEcoCoins())
                .paymentStatus(entity.getPaymentStatus())
                .build();
    }
}