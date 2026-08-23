package com.example.green.api.controller;

import com.example.green.api.dto.request.ProfileRequestDto;
import com.example.green.api.dto.response.ProfileResponseDto;
import com.example.green.service.ProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ProfileResponseDto getMyProfile() {
        return profileService.getMyProfile();
    }

    @PutMapping("/me")
    public ProfileResponseDto updateMyProfile(@Valid @RequestBody ProfileRequestDto request) {
        return profileService.upsertMyProfile(request);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProfileResponseDto getByUserId(@PathVariable @Positive Long userId) {
        return profileService.getByUserId(userId);
    }
}