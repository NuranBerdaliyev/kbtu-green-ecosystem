package com.example.green.service;

import com.example.green.api.dto.request.TripRequestDto;
import com.example.green.api.dto.response.TripResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.TripMapper;
import com.example.green.domain.entity.Trip;
import com.example.green.domain.entity.User;
import com.example.green.domain.repository.TripRepository;
import com.example.green.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripMapper tripMapper;

    public List<TripResponseDto> findAll() {
        return tripRepository.findAll().stream()
                .map(tripMapper::toDto)
                .toList();
    }

    public TripResponseDto findById(Long id) {
        return tripMapper.toDto(getTripOrThrow(id));
    }

    public TripResponseDto create(TripRequestDto request) {
        User driver = getUserOrThrow(request.getDriverId());
        Trip saved = tripRepository.save(tripMapper.toEntity(request, driver));
        return tripMapper.toDto(saved);
    }

    public TripResponseDto update(Long id, TripRequestDto request) {
        Trip entity = getTripOrThrow(id);
        User driver = getUserOrThrow(request.getDriverId());
        tripMapper.updateEntity(entity, request, driver);
        Trip saved = tripRepository.save(entity);
        return tripMapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!tripRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trip not found: id=" + id);
        }
        tripRepository.deleteById(id);
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
