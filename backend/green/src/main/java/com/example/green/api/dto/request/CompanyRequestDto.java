package com.example.green.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompanyRequestDto {

    @NotBlank
    @Size(max = 255)
    private String name;

    private String description;

    @Size(max = 500)
    private String website;

    private Boolean isPartner;
}