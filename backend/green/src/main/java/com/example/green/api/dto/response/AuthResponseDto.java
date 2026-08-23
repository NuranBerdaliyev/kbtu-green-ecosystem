package com.example.green.api.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDto {

    private String tokenType;      // обычно "Bearer"
    private String accessToken;
    private long accessExpiresIn;  // секунды
    private String refreshToken;
    private long refreshExpiresIn; // секунды
    private Long userId;
    private String email;
    private String role;
}
