package com.example.green.api.controller;

import com.example.green.api.dto.response.TripParticipantResponseDto;
import com.example.green.service.TripParticipantService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carpool")
@RequiredArgsConstructor
@Validated
public class TripParticipantController {
    private final TripParticipantService tripParticipantService;
    @PostMapping("trips/{tripId}/participants/join")
    @ResponseStatus(HttpStatus.CREATED)
    public TripParticipantResponseDto join(@PathVariable @Positive Long tripId) {
        return tripParticipantService.joinTrip(tripId);
    }

    @DeleteMapping("trips/{tripId}/participants/leave")
    public TripParticipantResponseDto leave(@PathVariable @Positive Long tripId) {
        return tripParticipantService.leaveTrip(tripId);
    }
}
