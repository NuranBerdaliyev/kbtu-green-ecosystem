package com.example.green.domain.repository;

import com.example.green.domain.entity.Trip;
import com.example.green.domain.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByTripStatusAndDepartureTimeAfter(TripStatus status, LocalDateTime time);
}
