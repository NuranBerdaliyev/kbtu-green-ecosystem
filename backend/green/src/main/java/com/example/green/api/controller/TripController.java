package com.example.green.api.controller;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.domain.entity.Trip;
import com.example.green.domain.repository.TripRepository;
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
    private final TripRepository repository;

    @GetMapping
    public List<Trip> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Trip findById(@PathVariable @Positive Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: id=" + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Trip create(@RequestBody @Valid Trip request) {
        request.setId(null);
        return repository.save(request);
    }

    @PutMapping("/{id}")
    public Trip update(@PathVariable @Positive Long id, @RequestBody @Valid Trip request) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Trip not found: id=" + id);
        }
        request.setId(id);
        return repository.save(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Trip not found: id=" + id);
        }
        repository.deleteById(id);
    }
}
