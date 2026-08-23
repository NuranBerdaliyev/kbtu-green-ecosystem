package com.example.green.service;

import com.example.green.api.dto.response.TripParticipantResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.TripParticipantMapper;
import com.example.green.domain.entity.Trip;
import com.example.green.domain.entity.TripParticipant;
import com.example.green.domain.entity.User;
import com.example.green.domain.repository.TripParticipantRepository;
import com.example.green.domain.repository.TripRepository;
import com.example.green.domain.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.green.domain.enums.TripStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripParticipantService {
    private final TripParticipantRepository tripParticipantRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripParticipantMapper tripParticipantMapper;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<TripParticipantResponseDto> getActiveParticipants(
            Long tripId
    ) {
        getTripOrThrow(tripId);

        return tripParticipantRepository
                .findByTripIdAndIsCancelledFalse(tripId)
                .stream()
                .map(tripParticipantMapper::toDto)
                .toList();
    }

    @Transactional
    public TripParticipantResponseDto joinTrip(Long tripId) {
        Trip trip = getTripOrThrow(tripId);
        User passenger = currentUserService.getCurrentUserOrThrow();

        if (trip.getTripStatus() != TripStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only active trips can be joined"
            );
        }

        if (trip.getDriver().getId().equals(
                passenger.getId()
        )) {
            throw new IllegalStateException(
                    "Driver cannot join own trip as passenger"
            );
        }

        TripParticipant existingParticipant =
                tripParticipantRepository
                        .findByTripIdAndPassengerId(
                                tripId,
                                passenger.getId()
                        )
                        .orElse(null);

        if (existingParticipant != null) {
            if (!Boolean.TRUE.equals(
                    existingParticipant.getIsCancelled()
            )) {
                throw new IllegalStateException(
                        "Passenger already joined this trip"
                );
            }

            trip.occupySeat();
            existingParticipant.setIsCancelled(false);
            existingParticipant.setJoinedAt(
                    java.time.LocalDateTime.now()
            );

            tripRepository.save(trip);

            return tripParticipantMapper.toDto(
                    tripParticipantRepository.save(
                            existingParticipant
                    )
            );
        }

        trip.occupySeat();

        TripParticipant participant =
                tripParticipantMapper.toEntity(
                        trip,
                        passenger
                );

        TripParticipant saved =
                tripParticipantRepository.save(participant);

        tripRepository.save(trip);

        return tripParticipantMapper.toDto(saved);
    }
    @Transactional
    public TripParticipantResponseDto leaveTrip(Long tripId) {
        User passenger = currentUserService.getCurrentUserOrThrow();

        TripParticipant participant = tripParticipantRepository
                .findByTripIdAndPassengerId(tripId, passenger.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        Trip trip = participant.getTrip();
        trip.validateMutable();

        if (Boolean.TRUE.equals(participant.getIsCancelled())) {
            throw new IllegalStateException("Participation already cancelled");
        }

        participant.setIsCancelled(true);
        trip.releaseSeat();

        tripRepository.save(trip);
        return tripParticipantMapper.toDto(tripParticipantRepository.save(participant));
    }

    private Trip getTripOrThrow(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: id=" + id));
    }
}
