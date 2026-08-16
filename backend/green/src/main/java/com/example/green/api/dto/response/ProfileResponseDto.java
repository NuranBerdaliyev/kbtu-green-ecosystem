package com.example.green.api.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseDto {
    private Long id;
    private Long userId;
    private String phone;
    private String avatarUrl;
    private String bio;
    private LocalDate birthDate;
    private LocalDateTime updatedAt;
}
