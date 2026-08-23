package com.example.green.api.mapper;

import com.example.green.api.dto.request.ProfileRequestDto;
import com.example.green.api.dto.response.ProfileResponseDto;
import com.example.green.domain.entity.Profile;
import com.example.green.domain.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ProfileMapper {
    public ProfileResponseDto toDto(Profile profile) {
        return ProfileResponseDto.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .phone(profile.getPhone())
                .avatarUrl(profile.getAvatarUrl())
                .bio(profile.getBio())
                .birthDate(profile.getBirthDate())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    public Profile toEntity(ProfileRequestDto request, User user) {
        return Profile.builder()
                .user(user)
                .phone(request.getPhone())
                .avatarUrl(request.getAvatarUrl())
                .bio(request.getBio())
                .birthDate(request.getBirthDate())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void updateEntity(Profile profile, ProfileRequestDto request) {
        profile.setPhone(request.getPhone());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setBio(request.getBio());
        profile.setBirthDate(request.getBirthDate());
        profile.setUpdatedAt(LocalDateTime.now());
    }
}
