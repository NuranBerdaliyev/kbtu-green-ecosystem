package com.example.green.api.controller;

import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.domain.entity.EcoPointContainer;
import com.example.green.domain.repository.EcoPointContainerRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eco-point-containers")
@RequiredArgsConstructor
@Validated
public class EcoPointController {
    private final EcoPointContainerRepository repository;

    @GetMapping
    public List<EcoPointContainer> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public EcoPointContainer findById(@PathVariable @Positive Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EcoPointContainer not found: id=" + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EcoPointContainer create(@RequestBody @Valid EcoPointContainer request) {
        request.setId(null);
        return repository.save(request);
    }

    @PutMapping("/{id}")
    public EcoPointContainer update(@PathVariable @Positive Long id, @RequestBody @Valid EcoPointContainer request) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("EcoPointContainer not found: id=" + id);
        }
        request.setId(id);
        return repository.save(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("EcoPointContainer not found: id=" + id);
        }
        repository.deleteById(id);
    }
}
