package com.example.green.service;

import com.example.green.api.dto.request.LoginRequestDto;
import com.example.green.api.dto.request.RefreshTokenRequestDto;
import com.example.green.api.dto.request.RegisterRequestDto;
import com.example.green.api.dto.response.AuthResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.config.AuthProperties;
import com.example.green.domain.entity.Authentication;
import com.example.green.domain.entity.User;
import com.example.green.domain.repository.AuthenticationRepository;
import com.example.green.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationRepository authenticationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthProperties authProperties;

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Email already exists");
        });

        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(request.getRole())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .ecoCoinsBalance(0L)
                .esgRating(0)
                .totalCo2Saved(java.math.BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);
        String access = jwtService.generateAccessToken(saved);
        Authentication refresh = createAuthentication(saved);

        return toAuthResponse(saved, access, refresh.getToken());
    }

    @Transactional
    public AuthResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String access = jwtService.generateAccessToken(user);
        Authentication refresh = createAuthentication(user);

        return toAuthResponse(user, access, refresh.getToken());
    }

    @Transactional
    public AuthResponseDto refresh(RefreshTokenRequestDto request) {
        Authentication oldToken = authenticationRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        if (Boolean.TRUE.equals(oldToken.getRevoked())) {
            throw new BadCredentialsException("Refresh token revoked");
        }

        if (oldToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Refresh token expired");
        }

        oldToken.setRevoked(true);
        authenticationRepository.save(oldToken);

        User user = oldToken.getUser();
        String access = jwtService.generateAccessToken(user);
        Authentication newRefresh = createAuthentication(user);

        return toAuthResponse(user, access, newRefresh.getToken());
    }

    private Authentication createAuthentication(User user) {
        Authentication token = Authentication.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(authProperties.getRefreshTtlSeconds()))
                .revoked(false)
                .build();

        return authenticationRepository.save(token);
    }

    private AuthResponseDto toAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponseDto.builder()
                .tokenType("Bearer")
                .accessToken(accessToken)
                .accessExpiresIn(authProperties.getAccessTtlSeconds())
                .refreshToken(refreshToken)
                .refreshExpiresIn(authProperties.getRefreshTtlSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
