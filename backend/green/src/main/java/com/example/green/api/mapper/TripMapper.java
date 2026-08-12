package com.example.green.api.mapper;

import com.example.green.api.dto.request.TripRequestDto;
import com.example.green.api.dto.response.TripResponseDto;
import com.example.green.domain.entity.Trip;
import com.example.green.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TripMapper {
    private final GeometryMapper geometryMapper;

    public Trip toEntity(TripRequestDto dto, User driver) {
        return Trip.builder()
                .driver(driver)
                .departureLocation(geometryMapper.fromWkt(dto.getDepartureLocationWkt()))
                .departureTime(dto.getDepartureTime())
                .totalSeats(dto.getTotalSeats())
                .availableSeats(dto.getAvailableSeats())
                .tripStatus(dto.getTripStatus())
                .build();
    }

    public void updateEntity(Trip entity, TripRequestDto dto, User driver) {
        entity.setDriver(driver);
        entity.setDepartureLocation(geometryMapper.fromWkt(dto.getDepartureLocationWkt()));
        entity.setDepartureTime(dto.getDepartureTime());
        entity.setTotalSeats(dto.getTotalSeats());
        entity.setAvailableSeats(dto.getAvailableSeats());
        entity.setTripStatus(dto.getTripStatus());
    }

    public TripResponseDto toDto(Trip entity) {
        return TripResponseDto.builder()
                .id(entity.getId())
                .driverId(entity.getDriver().getId())
                .departureLocationWkt(geometryMapper.toWkt(entity.getDepartureLocation()))
                .departureTime(entity.getDepartureTime())
                .totalSeats(entity.getTotalSeats())
                .availableSeats(entity.getAvailableSeats())
                .tripStatus(entity.getTripStatus())
                .build();
    }
}
