package com.example.green.api.controller;

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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trip-participants")
@RequiredArgsConstructor
@Validated
public class TripParticipantController {
    private final TripParticipantRepository tripParticipantRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripParticipantMapper tripParticipantMapper;

    @GetMapping
    public List<TripParticipantResponseDto> findAll() {
        return tripParticipantRepository.findAll().stream()
                .map(tripParticipantMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public TripParticipantResponseDto findById(@PathVariable @Positive Long id) {
        TripParticipant entity = tripParticipantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TripParticipant not found: id=" + id));
        return tripParticipantMapper.toDto(entity);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripParticipantResponseDto create(@RequestBody @Valid TripParticipantRequestDto request) {
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: id=" + request.getTripId()));

        User passenger = userRepository.findById(request.getPassengerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + request.getPassengerId()));

        TripParticipant saved = tripParticipantRepository.save(tripParticipantMapper.toEntity(request, trip, passenger));
        return tripParticipantMapper.toDto(saved);
    }

    @PutMapping("/{id}")
    public TripParticipantResponseDto update(@PathVariable @Positive Long id,
                                             @RequestBody @Valid TripParticipantRequestDto request) {
        TripParticipant entity = tripParticipantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TripParticipant not found: id=" + id));

        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: id=" + request.getTripId()));

        User passenger = userRepository.findById(request.getPassengerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + request.getPassengerId()));

        tripParticipantMapper.updateEntity(entity, request, trip, passenger);
        TripParticipant saved = tripParticipantRepository.save(entity);
        return tripParticipantMapper.toDto(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        if (!tripParticipantRepository.existsById(id)) {
            throw new ResourceNotFoundException("TripParticipant not found: id=" + id);
        }
        tripParticipantRepository.deleteById(id);
    }
}
