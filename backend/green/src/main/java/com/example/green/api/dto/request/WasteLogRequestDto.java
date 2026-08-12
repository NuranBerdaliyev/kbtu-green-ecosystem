package com.example.green.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WasteLogRequestDto {
    @NotNull
    private Long userId;

    @NotNull
    private Long ecoPointContainerId;

    @NotNull
    private LocalDateTime scannedAt;

    @NotNull
    @Min(0)
    private Integer ecoCoinsEarned;
}
