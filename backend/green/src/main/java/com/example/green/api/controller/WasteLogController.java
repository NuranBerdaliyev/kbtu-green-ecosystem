package com.example.green.api.controller;

import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.domain.entity.WasteLog;
import com.example.green.domain.repository.WasteLogRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/waste-logs")
@RequiredArgsConstructor
@Validated
public class WasteLogController {
    private final WasteLogRepository repository;

    @GetMapping
    public List<WasteLog> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public WasteLog findById(@PathVariable @Positive Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WasteLog not found: id=" + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WasteLog create(@RequestBody @Valid WasteLog request) {
        request.setId(null);
        return repository.save(request);
    }

    @PutMapping("/{id}")
    public WasteLog update(@PathVariable @Positive Long id, @RequestBody @Valid WasteLog request) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("WasteLog not found: id=" + id);
        }
        request.setId(id);
        return repository.save(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("WasteLog not found: id=" + id);
        }
        repository.deleteById(id);
    }

}
