package com.example.green.api.controller;

import com.example.green.api.dto.response.TripParticipantResponseDto;
import com.example.green.service.TripParticipantService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/carpool/trips")
@RequiredArgsConstructor
@Validated
public class TripParticipantController {
    private final TripParticipantService tripParticipantService;

    @GetMapping("/{tripId}/participants")
    public List<TripParticipantResponseDto> getParticipants(
            @PathVariable @Positive Long tripId
    ) {
        return tripParticipantService
                .getActiveParticipants(tripId);
    }

    @PostMapping("/{tripId}/participants/join")
    @ResponseStatus(HttpStatus.CREATED)
    public TripParticipantResponseDto join(@PathVariable @Positive Long tripId) {
        return tripParticipantService.joinTrip(tripId);
    }

    @DeleteMapping("/{tripId}/participants/leave")
    public TripParticipantResponseDto leave(@PathVariable @Positive Long tripId) {
        return tripParticipantService.leaveTrip(tripId);
    }
}
