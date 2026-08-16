package com.example.green.api.controller;

import com.example.green.api.dto.request.ProfileRequestDto;
import com.example.green.api.dto.response.ProfileResponseDto;
import com.example.green.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/{userId}")
    public ProfileResponseDto getByUserId(@PathVariable Long userId) {
        return profileService.getByUserId(userId);
    }

    @PutMapping("/{userId}")
    public ProfileResponseDto upsertByUserId(@PathVariable Long userId,
                                             @Valid @RequestBody ProfileRequestDto request) {
        return profileService.upsertByUserId(userId, request);
    }
}