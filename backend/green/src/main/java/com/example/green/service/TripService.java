package com.example.green.service;

import com.example.green.api.dto.request.TripRequestDto;
import com.example.green.api.dto.request.TripSearchRequestDto;
import com.example.green.api.dto.response.TripResponseDto;
import com.example.green.api.error.ForbiddenException;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.GeometryMapper;
import com.example.green.api.mapper.TripMapper;
import com.example.green.domain.entity.Trip;
import com.example.green.domain.entity.User;
import com.example.green.domain.enums.TripStatus;
import com.example.green.domain.repository.TripParticipantRepository;
import com.example.green.domain.repository.TripRepository;
import com.example.green.domain.repository.UserRepository;
import com.example.green.service.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
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
    private final GeometryMapper geometryMapper;
    private final TripParticipantRepository tripParticipantRepository;
    private final EcoRewardService ecoRewardService;

    public List<TripResponseDto> findAllTrips() {
        return tripRepository.findAll().stream()
                .map(tripMapper::toDto)
                .toList();
    }

    public Page<TripResponseDto> search(TripSearchRequestDto request) {
        Sort sort = parseSort(request.getSort());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Point originPoint = buildOriginPoint(request);

        List<TripResponseDto> filtered = tripRepository.findAll(sort).stream()
                .filter(t -> t.getTripStatus() == TripStatus.ACTIVE)
                .filter(t -> request.getFromTime() == null || !t.getDepartureTime().isBefore(request.getFromTime()))
                .filter(t -> request.getToTime() == null || !t.getDepartureTime().isAfter(request.getToTime()))
                .filter(t -> request.getMinSeats() == null || t.getAvailableSeats() >= request.getMinSeats())
                .filter(t -> originPoint == null || request.getRadiusKm() == null
                        || GeoUtils.distanceKm(t.getDepartureLocation(), originPoint) <= request.getRadiusKm())
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
    @Transactional
    public TripResponseDto updateTrip(Long id, TripRequestDto request) {
        Trip entity = getTripOrThrow(id);
        requireDriver(entity);
        entity.validateMutable();
        int occupiedSeats = entity.getTotalSeats() - entity.getAvailableSeats();

        if (request.getTotalSeats() < occupiedSeats) {
            throw new IllegalStateException(
                    "Total seats cannot be less than occupied seats"
            );
        }

        tripMapper.updateEntityWithoutDriverAndStatus(entity, request);
        entity.setAvailableSeats(request.getTotalSeats() - occupiedSeats);
        Trip saved = tripRepository.save(entity);

        return tripMapper.toDto(saved);
    }

    public void deleteTrip(Long id) {
        Trip trip = getTripOrThrow(id);
        requireDriver(trip);
        if (trip.getTripStatus() != TripStatus.CREATED) {
            throw new IllegalStateException(
                    "Only CREATED trips can be deleted"
            );
        }
        tripRepository.delete(trip);
    }
    @Transactional
    public TripResponseDto activateStatus(Long id) {
        Trip trip = getTripOrThrow(id);
        requireDriver(trip);
        trip.changeStatus(TripStatus.ACTIVE);
        return tripMapper.toDto(tripRepository.save(trip));
    }
    @Transactional
    public TripResponseDto cancelStatus (Long id) {
        Trip trip = getTripOrThrow(id);
        requireDriver(trip);
        trip.changeStatus(TripStatus.CANCELLED);
        return tripMapper.toDto(tripRepository.save(trip));
    }
    @Transactional
    public TripResponseDto completeStatus(Long tripId) {
        Trip trip = getTripOrThrow(tripId);
        requireDriver(trip);

        trip.changeStatus(TripStatus.COMPLETED);
        tripRepository.save(trip);

        double distanceKm = GeoUtils.distanceKm(trip.getDepartureLocation(), trip.getDestinationLocation());

        ecoRewardService.rewardForTripDistance(trip.getDriver(), distanceKm, trip.getId());

        tripParticipantRepository.findByTripIdAndIsCancelledFalse(trip.getId())
                .forEach(p -> ecoRewardService.rewardForTripDistance(p.getPassenger(), distanceKm, trip.getId()));

        return tripMapper.toDto(trip);
    }

    private Point buildOriginPoint(TripSearchRequestDto request) {
        if (request.getOriginLat() == null || request.getOriginLng() == null) {
            return null;
        }
        String wkt = "POINT(" + request.getOriginLng() + " " + request.getOriginLat() + ")";
        return geometryMapper.fromWkt(wkt);
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

        Object principal = auth.getPrincipal();

        if (!(principal instanceof Long userId)) {
            throw new IllegalStateException("Invalid authentication principal");
        }

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found: id=" + userId));
    }

    private void requireDriver(Trip trip) {
        User currentUser = getCurrentUserOrThrow();

        if (!trip.getDriver().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the trip driver can perform this action");
        }
    }
}