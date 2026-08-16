package com.example.green.service;

import com.example.green.api.dto.request.TripRequestDto;
import com.example.green.api.dto.response.TripResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.TripMapper;
import com.example.green.domain.entity.Trip;
import com.example.green.domain.entity.User;
import com.example.green.domain.enums.TripStatus;
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

    public List<TripResponseDto> findAllTrips() {
        return tripRepository.findAll().stream()
                .map(tripMapper::toDto)
                .toList();
    }

    public TripResponseDto findTripById(Long id) {
        return tripMapper.toDto(getTripOrThrow(id));
    }

    public TripResponseDto createTrip(TripRequestDto request) {
        User driver = getUserOrThrow(request.getDriverId());
        Trip saved = tripRepository.save(tripMapper.toEntity(request, driver));
        return tripMapper.toDto(saved);
    }

    public TripResponseDto updateTrip(Long id, TripRequestDto request) {
        Trip entity = getTripOrThrow(id);
        entity.validateMutable();

        User driver = getUserOrThrow(request.getDriverId());
        if (entity.isPublished() && !entity.getDriver().getId().equals(driver.getId())) {
            throw new IllegalStateException("Driver cannot be changed after trip publication");
        }
        if (!entity.isPublished()) {
            entity.setDriver(driver);
        }

        tripMapper.updateEntityWithoutDriverAndStatus(entity, request);

        if (request.getTripStatus() != null && request.getTripStatus() != entity.getTripStatus()) {
            entity.changeStatus(request.getTripStatus());
        }
        Trip saved = tripRepository.save(entity);
        return tripMapper.toDto(saved);
    }

    public void deleteTrip(Long id) {
        if (!tripRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trip not found: id=" + id);
        }
        Trip trip = getTripOrThrow(id);
        trip.validateMutable();
        tripRepository.deleteById(id);
    }
    public TripResponseDto activateStatus(Long id) {
        Trip trip = getTripOrThrow(id);
        trip.changeStatus(TripStatus.ACTIVE);
        return tripMapper.toDto(tripRepository.save(trip));
    }

    public TripResponseDto completeStatus(Long id) {
        Trip trip = getTripOrThrow(id);
        trip.changeStatus(TripStatus.COMPLETED);
        return tripMapper.toDto(tripRepository.save(trip));
    }

    public TripResponseDto cancelStatus (Long id) {
        Trip trip = getTripOrThrow(id);
        trip.changeStatus(TripStatus.CANCELLED);
        return tripMapper.toDto(tripRepository.save(trip));
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
