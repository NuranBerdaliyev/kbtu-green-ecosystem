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
        return tripParticipantService.findAll();
    }

    @GetMapping("/{id}")
    public TripParticipantResponseDto findById(@PathVariable @Positive Long id) {
        return tripParticipantService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripParticipantResponseDto create(@RequestBody @Valid TripParticipantRequestDto request) {
        return tripParticipantService.create(request);
    }

    @PutMapping("/{id}")
    public TripParticipantResponseDto update(@PathVariable @Positive Long id,
                                             @RequestBody @Valid TripParticipantRequestDto request) {
        return tripParticipantService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        tripParticipantService.delete(id);
    }
}
