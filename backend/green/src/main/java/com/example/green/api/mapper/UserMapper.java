package com.example.green.api.mapper;

import com.example.green.api.dto.request.UserRequestDto;
import com.example.green.api.dto.response.UserResponseDto;
import com.example.green.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(UserRequestDto dto) {
        return User.builder()
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .role(dto.getRole())
                .ecoCoinsBalance(dto.getEcoCoinsBalance())
                .esgRating(dto.getEsgRating())
                .totalCo2Saved(dto.getTotalCo2Saved())
                .createdAt(dto.getCreatedAt())
                .build();
    }

    public void updateEntity(User entity, UserRequestDto dto) {
        entity.setEmail(dto.getEmail());
        entity.setFullName(dto.getFullName());
        entity.setRole(dto.getRole());
        entity.setEcoCoinsBalance(dto.getEcoCoinsBalance());
        entity.setEsgRating(dto.getEsgRating());
        entity.setTotalCo2Saved(dto.getTotalCo2Saved());
        entity.setCreatedAt(dto.getCreatedAt());
    }

    public UserResponseDto toDto(User entity) {
        return UserResponseDto.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .fullName(entity.getFullName())
                .role(entity.getRole())
                .ecoCoinsBalance(entity.getEcoCoinsBalance())
                .esgRating(entity.getEsgRating())
                .totalCo2Saved(entity.getTotalCo2Saved())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
