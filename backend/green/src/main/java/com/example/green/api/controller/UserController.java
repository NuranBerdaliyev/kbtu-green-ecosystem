package com.example.green.api.controller;

import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.domain.entity.User;
import com.example.green.domain.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserRepository repository;

    @GetMapping
    public List<User> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable @Positive Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody @Valid User request) {
        request.setId(null);
        return repository.save(request);
    }
    @PutMapping("/{id}")
    public User update(@PathVariable @Positive Long id, @RequestBody @Valid User request) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: id=" + id);
        }
        request.setId(id);
        return repository.save(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: id=" + id);
        }
        repository.deleteById(id);
    }
}
