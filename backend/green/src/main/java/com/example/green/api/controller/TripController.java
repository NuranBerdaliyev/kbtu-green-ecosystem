package com.example.green.api.controller;
import com.example.green.api.dto.request.TripRequestDto;
import com.example.green.api.dto.response.TripResponseDto;
import com.example.green.service.TripService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Validated
public class TripController {
    private final TripService tripService;

    @GetMapping
    public List<TripResponseDto> findAll() {
        return tripService.findAllTrips();
    }

    @GetMapping("/{id}")
    public TripResponseDto findById(@PathVariable @Positive Long id) {
        return tripService.findTripById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponseDto create(@RequestBody @Valid TripRequestDto request) {
        return tripService.createTrip(request);
    }

    @PutMapping("/{id}")
    public TripResponseDto update(@PathVariable @Positive Long id,
                                  @RequestBody @Valid TripRequestDto request) {
        return tripService.updateTrip(id, request);
    }

    @PatchMapping("/{id}/activate")
    public TripResponseDto activate(@PathVariable @Positive Long id) {
        return tripService.activateStatus(id);
    }

    @PatchMapping("/{id}/complete")
    public TripResponseDto complete(@PathVariable @Positive Long id) {
        return tripService.completeStatus(id);
    }

    @PatchMapping("/{id}/cancel")
    public TripResponseDto cancel(@PathVariable @Positive Long id) {
        return tripService.cancelStatus(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        tripService.deleteTrip(id);
    }
}
