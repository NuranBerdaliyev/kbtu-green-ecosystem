package com.example.green.api.dto.request;

import com.example.green.domain.enums.Role;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserRequestDto {
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(max = 255)
    private String fullName;

    @NotNull
    private Role role;

    @NotNull
    @Min(0)
    private Long ecoCoinsBalance;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer esgRating;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 12, fraction = 3)
    private BigDecimal totalCo2Saved;

    @NotNull
    private LocalDateTime createdAt;
}
