package com.example.green.service;

import com.example.green.api.dto.request.UserRequestDto;
import com.example.green.api.dto.response.UserResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.UserMapper;
import com.example.green.domain.entity.User;
import com.example.green.domain.enums.Role;
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
    private final CurrentUserService currentUserService;

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
    public UserResponseDto update(Long id, UserRequestDto request) {
        User currentAdmin = currentUserService.getCurrentUserOrThrow();
        User target = getUserOrThrow(id);

        boolean removesAdminRole = target.getRole() == Role.ADMIN && request.getRole() != Role.ADMIN;

        if (removesAdminRole && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new IllegalStateException("The last administrator cannot be demoted");
        }
        if (currentAdmin.getId().equals(target.getId()) && request.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Administrator cannot demote their own account");
        }

            userMapper.updateEntity(target, request);

        return userMapper.toDto(
                userRepository.save(target)
        );
    }

    @Transactional
    public void delete(Long id) {
        User currentAdmin = currentUserService.getCurrentUserOrThrow();
        User target = getUserOrThrow(id);

        if (currentAdmin.getId().equals(target.getId())) {
            throw new IllegalStateException("Administrator cannot delete their own account");
        }

        if (target.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new IllegalStateException("The last administrator cannot be deleted");
        }

        userRepository.delete(target);
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + id));
    }
}