package com.example.green.api.mapper;

import com.example.green.api.dto.request.TripParticipantRequestDto;
import com.example.green.api.dto.response.TripParticipantResponseDto;
import com.example.green.domain.entity.Trip;
import com.example.green.domain.entity.TripParticipant;
import com.example.green.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TripParticipantMapper {
    public TripParticipant toEntity(TripParticipantRequestDto dto, Trip trip, User passenger) {
        return TripParticipant.builder()
                .trip(trip)
                .passenger(passenger)
                .joinedAt(dto.getJoinedAt())
                .isCancelled(dto.getIsCancelled())
                .build();
    }

    public void updateEntity(TripParticipant entity, TripParticipantRequestDto dto, Trip trip, User passenger) {
        entity.setTrip(trip);
        entity.setPassenger(passenger);
        entity.setJoinedAt(dto.getJoinedAt());
        entity.setIsCancelled(dto.getIsCancelled());
    }

    public TripParticipantResponseDto toDto(TripParticipant entity) {
        return TripParticipantResponseDto.builder()
                .id(entity.getId())
                .tripId(entity.getTrip().getId())
                .passengerId(entity.getPassenger().getId())
                .joinedAt(entity.getJoinedAt())
                .isCancelled(entity.getIsCancelled())
                .build();
    }
}
