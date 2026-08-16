package com.example.green.api.controller;

import com.example.green.api.dto.request.TripParticipantRequestDto;
import com.example.green.api.dto.response.TripParticipantResponseDto;
import com.example.green.service.TripParticipantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trip-participants")
@RequiredArgsConstructor
@Validated
public class TripParticipantController {
    private final TripParticipantService tripParticipantService;

    @GetMapping
    public List<TripParticipantResponseDto> findAll() {
        return tripParticipantService.findAllTripParticipants();
    }

    @GetMapping("/{id}")
    public TripParticipantResponseDto findById(@PathVariable @Positive Long id) {
        return tripParticipantService.findTripParticipantById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripParticipantResponseDto create(@RequestBody @Valid TripParticipantRequestDto request) {
        return tripParticipantService.createTripParticipant(request);
    }

    @PutMapping("/{id}")
    public TripParticipantResponseDto update(@PathVariable @Positive Long id,
                                             @RequestBody @Valid TripParticipantRequestDto request) {
        return tripParticipantService.updateTripParticipant(id, request);
    }
    @PatchMapping("/trips/{tripId}/passengers/{passengerId}/cancel")
    public TripParticipantResponseDto cancelParticipation(
            @PathVariable @Positive Long tripId,
            @PathVariable @Positive Long passengerId
    ) {
        return tripParticipantService.cancelParticipation(tripId, passengerId);
    }
}
