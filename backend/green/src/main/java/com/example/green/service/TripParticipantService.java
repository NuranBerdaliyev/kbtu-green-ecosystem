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

    public List<TripParticipantResponseDto> findAll() {
        return tripParticipantRepository.findAll().stream()
                .map(tripParticipantMapper::toDto)
                .toList();
    }

    public TripParticipantResponseDto findById(Long id) {
        return tripParticipantMapper.toDto(getTripParticipantOrThrow(id));
    }

    public TripParticipantResponseDto create(TripParticipantRequestDto request) {
        Trip trip = getTripOrThrow(request.getTripId());
        User passenger = getUserOrThrow(request.getPassengerId());
        TripParticipant saved = tripParticipantRepository.save(tripParticipantMapper.toEntity(request, trip, passenger));
        return tripParticipantMapper.toDto(saved);
    }

    public TripParticipantResponseDto update(Long id, TripParticipantRequestDto request) {
        TripParticipant entity = getTripParticipantOrThrow(id);
        Trip trip = getTripOrThrow(request.getTripId());
        User passenger = getUserOrThrow(request.getPassengerId());
        tripParticipantMapper.updateEntity(entity, request, trip, passenger);
        TripParticipant saved = tripParticipantRepository.save(entity);
        return tripParticipantMapper.toDto(saved);
    }
    public void delete(Long id) {
        if (!tripParticipantRepository.existsById(id)) {
            throw new ResourceNotFoundException("TripParticipant not found: id=" + id);
        }
        tripParticipantRepository.deleteById(id);
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
