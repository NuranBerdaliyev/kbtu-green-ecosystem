package com.example.green.service;
import com.example.green.api.dto.request.ProfileRequestDto;
import com.example.green.api.dto.response.ProfileResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.ProfileMapper;
import com.example.green.domain.entity.Profile;
import com.example.green.domain.entity.User;
import com.example.green.domain.repository.ProfileRepository;
import com.example.green.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;

    @Transactional(readOnly = true)
    public ProfileResponseDto getByUserId(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for userId=" + userId));
        return profileMapper.toDto(profile);
    }

    @Transactional
    public ProfileResponseDto upsertByUserId(Long userId, ProfileRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + userId));

        Profile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> Profile.builder().user(user).build());


        profileMapper.updateEntity(profile, request);
        Profile saved = profileRepository.save(profile);
        return profileMapper.toDto(saved);
    }
}
