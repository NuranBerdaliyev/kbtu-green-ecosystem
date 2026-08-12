package com.example.green.api.controller;

import com.example.green.api.dto.request.UserRequestDto;
import com.example.green.api.dto.response.UserResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.UserMapper;
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
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping
    public List<UserResponseDto> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public UserResponseDto findById(@PathVariable @Positive Long id) {
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + id));
        return userMapper.toDto(entity);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto create(@RequestBody @Valid UserRequestDto request) {
        User saved = userRepository.save(userMapper.toEntity(request));
        return userMapper.toDto(saved);
    }

    @PutMapping("/{id}")
    public UserResponseDto update(@PathVariable @Positive Long id,
                                  @RequestBody @Valid UserRequestDto request) {
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + id));

        userMapper.updateEntity(entity, request);
        User saved = userRepository.save(entity);
        return userMapper.toDto(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: id=" + id);
        }
        userRepository.deleteById(id);
    }
}
