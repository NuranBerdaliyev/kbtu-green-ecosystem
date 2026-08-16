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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripParticipantService {
    private final TripParticipantRepository tripParticipantRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripParticipantMapper tripParticipantMapper;

    @Transactional
    public TripParticipantResponseDto joinTrip(Long tripId) {
        Trip trip = getTripOrThrow(tripId);
        User passenger = getCurrentUserOrThrow();

        if (trip.isTerminal()) {
            throw new IllegalStateException("Cannot join completed/cancelled trip");
        }
        if (tripParticipantRepository.existsByTripIdAndPassengerId(tripId, passenger.getId())) {
            throw new IllegalStateException("Passenger already joined this trip");
        }

        trip.occupySeat();

        TripParticipant saved = tripParticipantRepository.save(
                tripParticipantMapper.toEntity(trip, passenger)
        );
        tripRepository.save(trip);

        return tripParticipantMapper.toDto(saved);
    }
    @Transactional
    public TripParticipantResponseDto leaveTrip(Long tripId) {
        User passenger = getCurrentUserOrThrow();

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

    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }
        String login = auth.getName();
        return userRepository.findByEmail(login)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + login));
    }

    private Trip getTripOrThrow(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: id=" + id));
    }
}
