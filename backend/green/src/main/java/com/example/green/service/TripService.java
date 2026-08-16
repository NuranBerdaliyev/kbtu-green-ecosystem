package com.example.green.service;

import com.example.green.api.dto.request.TripRequestDto;
import com.example.green.api.dto.request.TripSearchRequestDto;
import com.example.green.api.dto.response.TripResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.TripMapper;
import com.example.green.domain.entity.Trip;
import com.example.green.domain.entity.User;
import com.example.green.domain.enums.TripStatus;
import com.example.green.domain.repository.TripRepository;
import com.example.green.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

    public Page<TripResponseDto> search(TripSearchRequestDto request) {
        Sort sort = parseSort(request.getSort());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        List<TripResponseDto> filtered = tripRepository.findAll().stream()
                .filter(t -> t.getTripStatus() == TripStatus.ACTIVE)
                .filter(t -> request.getFromTime() == null || !t.getDepartureTime().isBefore(request.getFromTime()))
                .filter(t -> request.getToTime() == null || !t.getDepartureTime().isAfter(request.getToTime()))
                .filter(t -> request.getMinSeats() == null || t.getAvailableSeats() >= request.getMinSeats())
                .map(tripMapper::toDto)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        List<TripResponseDto> content =
                start >= filtered.size() ? List.of() : filtered.subList(start, end);

        return new PageImpl<>(content, pageable, filtered.size());
    }

    public TripResponseDto findTripById(Long id) {
        return tripMapper.toDto(getTripOrThrow(id));
    }

    public TripResponseDto createTrip(TripRequestDto request) {
        User driver = getCurrentUserOrThrow();
        Trip saved = tripRepository.save(tripMapper.toEntity(request, driver));
        return tripMapper.toDto(saved);
    }

    public TripResponseDto updateTrip(Long id, TripRequestDto request) {
        Trip entity = getTripOrThrow(id);
        entity.validateMutable();

        tripMapper.updateEntityWithoutDriverAndStatus(entity, request);
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


    private Sort parseSort(String sortRaw) {
        if (sortRaw == null || sortRaw.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "departureTime");
        }

        String[] parts = sortRaw.split(",");
        String field = parts[0].trim();
        Sort.Direction direction =
                (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()))
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        return Sort.by(direction, field);
    }

    private Trip getTripOrThrow(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: id=" + id));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + id));
    }

    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        // Обычно тут email/username из JWT
        String login = auth.getName();

        return userRepository.findByEmail(login) // или findByUsername(login)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + login));
    }
}
