package com.example.green.api.controller;
import com.example.green.api.dto.request.TripRequestDto;
import com.example.green.api.dto.request.TripSearchRequestDto;
import com.example.green.api.dto.response.TripResponseDto;
import com.example.green.service.TripService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;
@RestController
@RequestMapping("/api/carpool/trips")
@RequiredArgsConstructor
@Validated
public class TripController {
    private final TripService tripService;

    // POST /api/carpool/trips — create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponseDto create(@RequestBody @Valid TripRequestDto request) {
        return tripService.createTrip(request);
    }

    // GET /api/carpool/trips/{tripId} — details
    @GetMapping("/{tripId}")
    public TripResponseDto details(@PathVariable @Positive Long tripId) {
        return tripService.findTripById(tripId);
    }

    // GET /api/carpool/trips/search — search (с пагинацией)
    @GetMapping("/search")
    public Page<TripResponseDto> search(@Valid @ModelAttribute TripSearchRequestDto request) {
        return tripService.search(request);
    }

    // POST /api/carpool/trips/{tripId}/complete — complete
    @PostMapping("/{tripId}/complete")
    public TripResponseDto complete(@PathVariable @Positive Long tripId) {
        return tripService.completeStatus(tripId);
    }

    // POST /api/carpool/trips/{tripId}/cancel — cancel trip
    @PostMapping("/{tripId}/cancel")
    public TripResponseDto cancel(@PathVariable @Positive Long tripId) {
        return tripService.cancelStatus(tripId);
    }

}
