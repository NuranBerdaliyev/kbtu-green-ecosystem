package com.example.green.service;

import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.domain.entity.User;
import com.example.green.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUserOrThrow() {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException(
                    "User is not authenticated"
            );
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof Long userId)) {
            throw new IllegalStateException(
                    "Invalid authentication principal"
            );
        }

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: id=" + userId
                        )
                );
    }
}