package com.example.green.service;

import com.example.green.api.dto.request.LoginRequestDto;
import com.example.green.api.dto.request.RefreshTokenRequestDto;
import com.example.green.api.dto.request.RegisterRequestDto;
import com.example.green.api.dto.response.AuthResponseDto;
import com.example.green.api.mapper.AuthMapper;
import com.example.green.config.AuthProperties;
import com.example.green.domain.entity.Authentication;
import com.example.green.domain.entity.User;
import com.example.green.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthMapper authMapper;
    private final AuthProperties authProperties;

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Email already exists");
        }); //checking if client wants to register while he is already registered.

        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(request.getRole())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .ecoCoinsBalance(0L)
                .esgRating(0)
                .totalCo2Saved(java.math.BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build(); //building default values for the new user

        User saved = userRepository.save(user);//saving the new user to database
        String access = jwtService.generateAccessToken(saved);//generating access token for the new user
        Authentication refresh = refreshTokenService.createRefreshToken(saved, authProperties.getRefreshTtlSeconds()); //Creating Authentication object for the new user
        // with refresh token

        return authMapper.toDto(saved, access, refresh.getToken(), authProperties.getAccessTtlSeconds(),
                authProperties.getRefreshTtlSeconds()); //Preparing Response from the server
    }

    @Transactional
    public AuthResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));//checking if the user exists

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");// checking if the password is right
        }

        String accessToken = jwtService.generateAccessToken(user);//access token
        Authentication newRefresh = refreshTokenService.createRefreshToken(user, authProperties.getRefreshTtlSeconds());//Authentication object with refresh token

        return authMapper.toDto(user,
                accessToken,
                newRefresh.getToken(),
                authProperties.getAccessTtlSeconds(),
                authProperties.getRefreshTtlSeconds());//Preparing the response from the server
    }

    @Transactional
    public AuthResponseDto refresh(RefreshTokenRequestDto request) {
        Authentication newRefresh = refreshTokenService.updateRefreshToken(
                request.getRefreshToken(),
                authProperties.getRefreshTtlSeconds()
        );

        User user = newRefresh.getUser();
        String accessToken = jwtService.generateAccessToken(user);
        //preparing response from the server
        return authMapper.toDto(user,
                accessToken,
                newRefresh.getToken(),
                authProperties.getAccessTtlSeconds(),
                authProperties.getRefreshTtlSeconds());
    }
}
