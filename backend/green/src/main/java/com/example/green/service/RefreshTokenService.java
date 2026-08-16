package com.example.green.service;

import com.example.green.domain.entity.Authentication;
import com.example.green.domain.entity.User;
import com.example.green.domain.repository.AuthenticationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final AuthenticationRepository authenticationRepository;

    @Transactional
    public Authentication createRefreshToken(User user, long refreshTtlSeconds) {
        Authentication token = Authentication.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTtlSeconds))
                .revoked(false)
                .build();

        return authenticationRepository.save(token);
    }

    @Transactional
    public Authentication updateRefreshToken(String rawRefreshToken, long refreshTtlSeconds) {
        Authentication oldToken = authenticationRepository.findByToken(rawRefreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (Boolean.TRUE.equals(oldToken.getRevoked())) {
            throw new BadCredentialsException("Refresh token revoked");
        }

        if (oldToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Refresh token expired");
        }

        oldToken.setRevoked(true);
        authenticationRepository.save(oldToken);

        return createRefreshToken(oldToken.getUser(), refreshTtlSeconds);
    }

    @Transactional
    public void deleteAllByUserId(Long userId) {
        authenticationRepository.deleteAllByUserId(userId);
    }
}
