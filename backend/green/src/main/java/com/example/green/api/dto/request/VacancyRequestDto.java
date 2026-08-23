package com.example.green.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VacancyRequestDto {
    @NotNull
    private Long companyId;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    private String description;
}