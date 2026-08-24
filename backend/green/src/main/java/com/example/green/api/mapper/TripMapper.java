package com.example.green.api.mapper;

import com.example.green.api.dto.request.TripRequestDto;
import com.example.green.api.dto.response.TripResponseDto;
import com.example.green.domain.entity.Trip;
import com.example.green.domain.entity.User;
import com.example.green.domain.enums.TripStatus;
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
                .destinationLocation(geometryMapper.fromWkt(dto.getDestinationLocationWkt()))
                .departureTime(dto.getDepartureTime())
                .totalSeats(dto.getTotalSeats())
                .availableSeats(dto.getTotalSeats())
                .tripStatus(TripStatus.CREATED)
                .priceEcoCoins(dto.getPriceEcoCoins())
                .build();
    }

    public void updateEntityWithoutDriverAndStatus(Trip entity, TripRequestDto dto) {
        entity.setDepartureLocation(geometryMapper.fromWkt(dto.getDepartureLocationWkt()));
        entity.setDestinationLocation(geometryMapper.fromWkt(dto.getDestinationLocationWkt()));
        entity.setDepartureTime(dto.getDepartureTime());
        entity.setTotalSeats(dto.getTotalSeats());
        entity.setPriceEcoCoins(dto.getPriceEcoCoins());
    }

    public TripResponseDto toDto(Trip entity) {
        return TripResponseDto.builder()
                .id(entity.getId())
                .driverId(entity.getDriver().getId())
                .departureLocationWkt(geometryMapper.toWkt(entity.getDepartureLocation()))
                .destinationLocationWkt(geometryMapper.toWkt(entity.getDestinationLocation()))
                .departureTime(entity.getDepartureTime())
                .totalSeats(entity.getTotalSeats())
                .availableSeats(entity.getAvailableSeats())
                .tripStatus(entity.getTripStatus())
                .priceEcoCoins(entity.getPriceEcoCoins())
                .build();
    }
}
