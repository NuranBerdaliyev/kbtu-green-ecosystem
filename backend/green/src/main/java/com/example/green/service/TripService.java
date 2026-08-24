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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.example.green.domain.entity.TripParticipant;
import com.example.green.domain.enums.TripPaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripMapper tripMapper;
    private final GeometryMapper geometryMapper;
    private final TripParticipantRepository tripParticipantRepository;
    private final GamificationService gamificationService;
    private final CurrentUserService currentUserService;

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
                .filter(t -> t.getTripStatus() == TripStatus.PUBLISHED)
                .filter(t -> t.getDepartureTime().isAfter(LocalDateTime.now()))
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
        Trip trip = getTripOrThrow(id);

        if (trip.getTripStatus() == TripStatus.CREATED) {
            requireDriver(trip);
        }

        return tripMapper.toDto(trip);
    }

    public List<TripResponseDto> findMyTrips() {
        User currentUser = currentUserService.getCurrentUserOrThrow();

        return tripRepository
                .findByDriverIdOrderByDepartureTimeDesc(
                        currentUser.getId()
                )
                .stream()
                .map(tripMapper::toDto)
                .toList();
    }

    public List<TripResponseDto> findJoinedTrips() {
        User currentUser = currentUserService.getCurrentUserOrThrow();

        return tripParticipantRepository
                .findByPassengerIdAndIsCancelledFalseOrderByJoinedAtDesc(
                        currentUser.getId()
                )
                .stream()
                .map(participant -> participant.getTrip())
                .map(tripMapper::toDto)
                .toList();
    }

    public TripResponseDto createTrip(TripRequestDto request) {
        User driver = currentUserService.getCurrentUserOrThrow();
        Trip saved = tripRepository.save(tripMapper.toEntity(request, driver));
        return tripMapper.toDto(saved);
    }
    @Transactional
    public TripResponseDto updateTrip(Long id, TripRequestDto request) {
        Trip entity = getTripOrThrow(id);
        requireDriver(entity);
        int occupiedSeats = entity.getTotalSeats() - entity.getAvailableSeats();

        if (request.getTotalSeats() < occupiedSeats) {
            throw new IllegalStateException(
                    "Total seats cannot be less than occupied seats"
            );
        }
        if (entity.getTripStatus() != TripStatus.CREATED) {
            throw new IllegalStateException(
                    "Only CREATED trips can be edited"
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
    public TripResponseDto publishStatus(Long tripId) {
        Trip trip = getTripForUpdateOrThrow(tripId);

        requireDriver(trip);

        if (!trip.getDepartureTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Expired trip cannot be published");
        }

        trip.changeStatus(TripStatus.PUBLISHED);
        return tripMapper.toDto(tripRepository.save(trip));
    }
    @Transactional
    public TripResponseDto startStatus(Long tripId) {
        Trip trip = getTripForUpdateOrThrow(tripId);
        requireDriver(trip);

        if (trip.getTripStatus() != TripStatus.PUBLISHED) {
            throw new IllegalStateException("Only PUBLISHED trip can be started");
        }

        if (trip.getDepartureTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Trip cannot be started before departure time");
        }

        List<TripParticipant> participants = tripParticipantRepository.findByTripIdAndIsCancelledFalse(tripId);

        if (participants.isEmpty()) {
            throw new IllegalStateException("Trip cannot be started without passengers");
        }

        boolean invalidPayment =
                participants.stream().anyMatch(participant ->
                        participant.getPaymentStatus() != TripPaymentStatus.RESERVED || participant.getReservedEcoCoins() <= 0);

        if (invalidPayment) {
            throw new IllegalStateException("Every active passenger must have a reserved fare");
        }

        trip.changeStatus(TripStatus.IN_PROGRESS);
        return tripMapper.toDto(tripRepository.save(trip));
    }
    @Transactional
    public TripResponseDto cancelStatus(Long tripId) {
        Trip trip = getTripForUpdateOrThrow(tripId);
        requireDriver(trip);

        if (trip.isTerminal()) {
            throw new IllegalStateException("Terminal trip cannot be cancelled");
        }

        List<TripParticipant> participants = tripParticipantRepository.findByTripIdAndIsCancelledFalse(tripId);

        for (TripParticipant participant : participants) {
            if (participant.getPaymentStatus() != TripPaymentStatus.RESERVED) {
                throw new IllegalStateException("Active passenger has invalid payment status");
            }

            gamificationService.refundCarpoolFare(
                    participant.getPassenger().getId(),
                    participant.getId(),
                    participant.getReservedEcoCoins()
            );

            participant.refundAndCancel();
        }

        tripParticipantRepository.saveAll(participants);
        trip.changeStatus(TripStatus.CANCELLED);
        return tripMapper.toDto(tripRepository.save(trip));
    }
    @Transactional
    public TripResponseDto completeStatus(Long tripId) {
        Trip trip = getTripForUpdateOrThrow(tripId);
        requireDriver(trip);

        if (trip.getTripStatus() != TripStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only IN_PROGRESS trip can be completed");
        }

        List<TripParticipant> participants = tripParticipantRepository.findByTripIdAndIsCancelledFalse(tripId);

        if (participants.isEmpty()) {
            throw new IllegalStateException("Trip cannot be completed without passengers");
        }

        for (TripParticipant participant : participants) {
            if (participant.getPaymentStatus() != TripPaymentStatus.RESERVED) {
                throw new IllegalStateException("Passenger fare is not reserved");
            }
        }

        long totalEarning = participants.stream()
                .mapToLong(TripParticipant::getReservedEcoCoins)
                .reduce(0L, Math::addExact);


        gamificationService.creditCarpoolEarning(
                trip.getDriver().getId(),
                trip.getId(),
                totalEarning
        );

        participants.forEach(TripParticipant::settle);
        tripParticipantRepository.saveAll(participants);

        double distanceKm = GeoUtils.distanceKm(
                trip.getDepartureLocation(),
                trip.getDestinationLocation()
        );
        gamificationService.recordCompletedTripActivity(
                trip.getDriver().getId(),
                trip.getId(),
                distanceKm
        );

        for (TripParticipant participant : participants) {
            gamificationService.recordCompletedTripActivity(
                    participant.getPassenger().getId(),
                    trip.getId(),
                    distanceKm
            );
        }

        trip.changeStatus(TripStatus.COMPLETED);
        return tripMapper.toDto(tripRepository.save(trip));
    }
    private Point buildOriginPoint(TripSearchRequestDto request) {
        boolean hasLat = request.getOriginLat() != null;
        boolean hasLng = request.getOriginLng() != null;
        boolean hasRadius = request.getRadiusKm() != null;

        if (hasLat != hasLng) {
            throw new IllegalArgumentException("originLat and originLng must be provided together");
        }

        if (hasRadius && !hasLat) {
            throw new IllegalArgumentException("originLat and originLng are required when radiusKm is provided");
        }

        if (!hasLat) {
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

    private Trip getTripForUpdateOrThrow(Long id) {
        return tripRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Trip not found: id=" + id)
                );
    }
    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + id));
    }

    private void requireDriver(Trip trip) {
        User currentUser = currentUserService.getCurrentUserOrThrow();

        if (!trip.getDriver().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the trip driver can perform this action");
        }
    }
}