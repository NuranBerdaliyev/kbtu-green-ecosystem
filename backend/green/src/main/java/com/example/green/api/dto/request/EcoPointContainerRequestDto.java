package com.example.green.api.dto.request;

import com.example.green.domain.enums.WasteType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EcoPointContainerRequestDto {
    @NotBlank
    @Size(max = 255)
    private String title;

    @NotNull
    private String locationWkt; // POINT(lon lat)

    @NotNull
    private WasteType wasteType;

    @NotNull
    @Min(1)
    private Integer capacityGrams;

    @NotNull
    private Boolean isActive;

    @NotBlank
    @Size(max = 255)
    private String qrCodeToken;
}
