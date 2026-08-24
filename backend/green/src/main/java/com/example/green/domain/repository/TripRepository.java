package com.example.green.domain.repository;

import com.example.green.domain.entity.Trip;
import com.example.green.domain.enums.TripStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByTripStatusAndDepartureTimeAfter(TripStatus status, LocalDateTime time);
    List<Trip> findByDriverIdOrderByDepartureTimeDesc(
            Long driverId
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select t
        from Trip t
        where t.id = :id
        """)
    Optional<Trip> findByIdForUpdate(
            @Param("id") Long id
    );
}
