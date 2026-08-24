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

    //PUT /api/carpool/trips/{tripId}
    @PutMapping("/{tripId}")
    public TripResponseDto update(
            @PathVariable @Positive Long tripId,
            @RequestBody @Valid TripRequestDto request
    ) {
        return tripService.updateTrip(tripId, request);
    }
    //DELETE /api/carpool/trips/{tripId}
    @DeleteMapping("/{tripId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable @Positive Long tripId
    ) {
        tripService.deleteTrip(tripId);
    }

    //GET /api/carpool/trips/my
    @GetMapping("/my")
    public List<TripResponseDto> myTrips() {
        return tripService.findMyTrips();
    }

    //GET /api/carpool/trips/joined
    @GetMapping("/joined")
    public List<TripResponseDto> joinedTrips() {
        return tripService.findJoinedTrips();
    }

    // GET /api/carpool/trips/search — search (с пагинацией)
    @GetMapping("/search")
    public Page<TripResponseDto> search(@Valid @ModelAttribute TripSearchRequestDto request) {
        return tripService.search(request);
    }

    @PostMapping("/{tripId}/publish")
    public TripResponseDto publish(@PathVariable @Positive Long tripId) {
        return tripService.publishStatus(tripId);
    }
    @PostMapping("/{tripId}/start")
    public TripResponseDto start(@PathVariable @Positive Long tripId) {
        return tripService.startStatus(tripId);
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
