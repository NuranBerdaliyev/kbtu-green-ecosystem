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
        return tripService.findAll();
    }

    @GetMapping("/{id}")
    public TripResponseDto findById(@PathVariable @Positive Long id) {
        return tripService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponseDto create(@RequestBody @Valid TripRequestDto request) {
        return tripService.create(request);
    }

    @PutMapping("/{id}")
    public TripResponseDto update(@PathVariable @Positive Long id,
                                  @RequestBody @Valid TripRequestDto request) {
        return tripService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        tripService.delete(id);
    }
}
