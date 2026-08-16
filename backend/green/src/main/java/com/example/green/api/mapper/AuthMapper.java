package com.example.green.api.mapper;

import com.example.green.api.dto.response.AuthResponseDto;
import com.example.green.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {
    public AuthResponseDto toResponse(
            User user,
            String accessToken,
            String refreshToken,
            long accessTtlSeconds,
            long refreshTtlSeconds
    ) {
        return AuthResponseDto.builder()
                .tokenType("Bearer")
                .accessToken(accessToken)
                .accessExpiresIn(accessTtlSeconds)
                .refreshToken(refreshToken)
                .refreshExpiresIn(refreshTtlSeconds)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}