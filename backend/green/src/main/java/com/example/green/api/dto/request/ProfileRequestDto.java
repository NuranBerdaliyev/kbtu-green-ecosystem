package com.example.green.api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileRequestDto {
    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String avatarUrl;

    @Size(max = 500)
    private String bio;

    private LocalDate birthDate;
}
