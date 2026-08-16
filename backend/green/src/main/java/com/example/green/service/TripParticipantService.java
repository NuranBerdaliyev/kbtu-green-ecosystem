package com.example.green.service;

import com.example.green.api.dto.request.TripParticipantRequestDto;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripParticipantService {
    private final TripParticipantRepository tripParticipantRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripParticipantMapper tripParticipantMapper;

    public List<TripParticipantResponseDto> findAllTripParticipants() {
        return tripParticipantRepository.findAll().stream()
                .map(tripParticipantMapper::toDto)
                .toList();
    }

    public TripParticipantResponseDto findTripParticipantById(Long id) {
        return tripParticipantMapper.toDto(getTripParticipantOrThrow(id));
    }

    @Transactional
    public TripParticipantResponseDto createTripParticipant(TripParticipantRequestDto request) {
        Trip trip = getTripOrThrow(request.getTripId());
        User passenger = getUserOrThrow(request.getPassengerId());
        if (trip.isTerminal()) {
            throw new IllegalStateException("Cannot join completed/cancelled trip");
        }

        if (tripParticipantRepository.existsByTripIdAndPassengerId(trip.getId(), passenger.getId())) {
            throw new IllegalStateException("Passenger already joined this trip");
        }

        trip.occupySeat();
        TripParticipant saved = tripParticipantRepository.save(tripParticipantMapper.toEntity(request, trip, passenger));
        tripRepository.save(trip);
        return tripParticipantMapper.toDto(saved);
    }

    @Transactional
    public TripParticipantResponseDto updateTripParticipant(Long id, TripParticipantRequestDto request) {
        TripParticipant entity = getTripParticipantOrThrow(id);
        entity.getTrip().validateMutable();
        tripParticipantMapper.updateEntity(entity, request);
        TripParticipant saved = tripParticipantRepository.save(entity);
        return tripParticipantMapper.toDto(saved);
    }

    @Transactional
    public TripParticipantResponseDto cancelParticipation(Long tripId, Long passengerId) {
        TripParticipant participant = tripParticipantRepository
                .findByTripIdAndPassengerId(tripId, passengerId)
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
    private TripParticipant getTripParticipantOrThrow(Long id) {
        return tripParticipantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TripParticipant not found: id=" + id));
    }

    private Trip getTripOrThrow(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: id=" + id));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + id));
    }
}
