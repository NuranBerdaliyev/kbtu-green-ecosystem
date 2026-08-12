package com.example.green.api.controller;
import com.example.green.api.dto.request.TripRequestDto;
import com.example.green.api.dto.response.TripResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.TripMapper;
import com.example.green.domain.entity.Trip;
import com.example.green.domain.entity.User;
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
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Validated
public class TripController {
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripMapper tripMapper;

    @GetMapping
    public List<TripResponseDto> findAll() {
        return tripRepository.findAll().stream()
                .map(tripMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public TripResponseDto findById(@PathVariable @Positive Long id) {
        Trip entity = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: id=" + id));
        return tripMapper.toDto(entity);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponseDto create(@RequestBody @Valid TripRequestDto request) {
        User driver = userRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + request.getDriverId()));

        Trip saved = tripRepository.save(tripMapper.toEntity(request, driver));
        return tripMapper.toDto(saved);
    }

    @PutMapping("/{id}")
    public TripResponseDto update(@PathVariable @Positive Long id,
                                  @RequestBody @Valid TripRequestDto request) {
        Trip entity = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: id=" + id));

        User driver = userRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + request.getDriverId()));

        tripMapper.updateEntity(entity, request, driver);
        Trip saved = tripRepository.save(entity);
        return tripMapper.toDto(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        if (!tripRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trip not found: id=" + id);
        }
        tripRepository.deleteById(id);
    }
}
