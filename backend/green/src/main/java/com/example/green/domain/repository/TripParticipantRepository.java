package com.example.green.domain.repository;

import com.example.green.domain.entity.TripParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripParticipantRepository extends JpaRepository<TripParticipant, Long> {
    List<TripParticipant> findByTripId(Long tripId);
    List<TripParticipant> findByPassengerId(Long passengerId);
}
