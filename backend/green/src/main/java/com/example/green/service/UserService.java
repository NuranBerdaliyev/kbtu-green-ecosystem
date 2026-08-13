package com.example.green.service;

import com.example.green.api.dto.request.UserRequestDto;
import com.example.green.api.dto.response.UserResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.UserMapper;
import com.example.green.domain.entity.User;
import com.example.green.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserResponseDto> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserResponseDto findById(Long id) {
        return userMapper.toDto(getUserOrThrow(id));
    }

    public UserResponseDto create(UserRequestDto request) {
        User saved = userRepository.save(userMapper.toEntity(request));
        return userMapper.toDto(saved);
    }

    public UserResponseDto update(Long id, UserRequestDto request) {
        User entity = getUserOrThrow(id);
        userMapper.updateEntity(entity, request);
        User saved = userRepository.save(entity);
        return userMapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: id=" + id);
        }
        userRepository.deleteById(id);
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + id));
    }
}
