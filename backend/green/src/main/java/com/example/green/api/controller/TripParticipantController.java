package com.example.green.api.controller;

import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.domain.entity.TripParticipant;
import com.example.green.domain.repository.TripParticipantRepository;
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
    private final TripParticipantRepository repository;

    @GetMapping
    public List<TripParticipant> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public TripParticipant findById(@PathVariable @Positive Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TripParticipant not found: id=" + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripParticipant create(@RequestBody @Valid TripParticipant request) {
        request.setId(null);
        return repository.save(request);
    }

    @PutMapping("/{id}")
    public TripParticipant update(@PathVariable @Positive Long id, @RequestBody @Valid TripParticipant request) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("TripParticipant not found: id=" + id);
        }
        request.setId(id);
        return repository.save(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("TripParticipant not found: id=" + id);
        }
        repository.deleteById(id);
    }
}
