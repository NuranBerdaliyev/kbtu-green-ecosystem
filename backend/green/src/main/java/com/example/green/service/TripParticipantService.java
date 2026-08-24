package com.example.green.service;

import com.example.green.api.dto.response.TripParticipantResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.TripParticipantMapper;
import com.example.green.domain.entity.Trip;
import com.example.green.domain.entity.TripParticipant;
import com.example.green.domain.entity.User;
import com.example.green.domain.enums.TripStatus;
import com.example.green.domain.repository.TripParticipantRepository;
import com.example.green.domain.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripParticipantService {

    private final TripParticipantRepository participantRepository;
    private final TripRepository tripRepository;
    private final TripParticipantMapper participantMapper;
    private final CurrentUserService currentUserService;
    private final GamificationService gamificationService;

    @Transactional(readOnly = true)
    public List<TripParticipantResponseDto> getActiveParticipants(Long tripId) {
        getTripOrThrow(tripId);

        return participantRepository
                .findByTripIdAndIsCancelledFalse(tripId)
                .stream()
                .map(participantMapper::toDto)
                .toList();
    }

    @Transactional
    public TripParticipantResponseDto joinTrip(Long tripId) {
        Trip trip = getTripForUpdateOrThrow(tripId);
        User passenger = currentUserService.getCurrentUserOrThrow();

        if (trip.getTripStatus() != TripStatus.PUBLISHED) {
            throw new IllegalStateException("Only PUBLISHED trips can be joined");
        }

        if (!trip.getDepartureTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Trip cannot be joined after departure time");
        }

        if (trip.getDriver().getId().equals(passenger.getId())) {
            throw new IllegalStateException("Driver cannot join own trip");
        }

        TripParticipant participant = participantRepository
                        .findByTripIdAndPassengerId(tripId, passenger.getId())
                        .orElse(null);

        trip.occupySeat();

        if (participant == null) {
            participant = participantMapper.toEntity(trip, passenger);
        } else {
            if (!Boolean.TRUE.equals(participant.getIsCancelled())) {
                throw new IllegalStateException("Passenger already joined this trip");
            }

            participant.rejoin(trip.getPriceEcoCoins());
        }

        /*
         * Нужен id участника для referenceId финансовой
         * операции.
         */
        participant = participantRepository.saveAndFlush(participant);

        gamificationService.reserveCarpoolFare(
                passenger.getId(),
                participant.getId(),
                participant.getReservedEcoCoins()
        );

        tripRepository.save(trip);

        return participantMapper.toDto(participant);
    }

    @Transactional
    public TripParticipantResponseDto leaveTrip(Long tripId) {
        Trip trip = getTripForUpdateOrThrow(tripId);
        User passenger = currentUserService.getCurrentUserOrThrow();

        if (trip.getTripStatus() != TripStatus.PUBLISHED) {
            throw new IllegalStateException("Trip can only be left while PUBLISHED");
        }

        TripParticipant participant =
                participantRepository
                        .findByTripIdAndPassengerId(tripId, passenger.getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Participant not found")
                        );

        if (Boolean.TRUE.equals(participant.getIsCancelled())) {
            throw new IllegalStateException("Participation already cancelled");
        }

        gamificationService.refundCarpoolFare(passenger.getId(), participant.getId(), participant.getReservedEcoCoins());
        participant.refundAndCancel();
        trip.releaseSeat();
        tripRepository.save(trip);

        return participantMapper.toDto(participantRepository.save(participant));
    }

    private Trip getTripForUpdateOrThrow(Long id) {
        return tripRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Trip not found: id=" + id)
                );
    }

    private Trip getTripOrThrow(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Trip not found: id=" + id)
                );
    }
}