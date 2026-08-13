package com.example.green.api.dto.response;

import com.example.green.domain.enums.Role;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserResponseDto {
    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private Long ecoCoinsBalance;
    private Integer esgRating;
    private BigDecimal totalCo2Saved;
    private LocalDateTime createdAt;
}
