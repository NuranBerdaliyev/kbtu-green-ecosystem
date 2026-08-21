package com.example.green.service;

import com.example.green.api.dto.request.UserRequestDto;
import com.example.green.api.dto.response.UserResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.UserMapper;
import com.example.green.domain.entity.User;
import com.example.green.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserResponseDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDto findById(Long id) {
        return userMapper.toDto(
                getUserOrThrow(id)
        );
    }

    @Transactional
    public UserResponseDto update(
            Long id,
            UserRequestDto request
    ) {
        User user = getUserOrThrow(id);
        userMapper.updateEntity(user, request);

        return userMapper.toDto(
                userRepository.save(user)
        );
    }

    @Transactional
    public void delete(Long id) {
        User user = getUserOrThrow(id);
        userRepository.delete(user);
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: id=" + id
                        )
                );
    }
}