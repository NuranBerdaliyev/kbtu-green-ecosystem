package com.example.green.api.controller;

import com.example.green.api.dto.request.LoginRequestDto;
import com.example.green.api.dto.request.RefreshTokenRequestDto;
import com.example.green.api.dto.request.RegisterRequestDto;
import com.example.green.api.dto.response.AuthResponseDto;
import com.example.green.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponseDto refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        return authService.refresh(request);
    }
}
