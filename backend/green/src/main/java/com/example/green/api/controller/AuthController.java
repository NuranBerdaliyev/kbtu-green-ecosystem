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
@RequestMapping("/auth")//endpoint parent to register, login and refresh the access token
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService; // AuthService is used here for adding new user, recognizing him
    // and refresh his access token

    @PostMapping("/register")//auth/register
    @ResponseStatus(HttpStatus.CREATED)//If this method is completed successfully, return 201 HTTP Status to client
    /*@Valid enables validation annotations in class fields which's object is given here as an argument
    * @RequestBody converts request body to RegisterRequestDto and initialize request object by this*/
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        return authService.register(request); //using AuthService methods for registering, login, refresh etc.
    }

    @PostMapping("/login")//auth/login
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return authService.login(request);
    }

    //This endpoint is called by frontend automatically when access token is expired.
    @PostMapping("/refresh")//auth/refresh
    public AuthResponseDto refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        return authService.refresh(request);
    }
}
