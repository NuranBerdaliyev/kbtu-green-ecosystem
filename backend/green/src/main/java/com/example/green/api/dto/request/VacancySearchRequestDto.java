package com.example.green.api.dto.request;

import jakarta.validation.constraints.Min;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VacancySearchRequestDto {
    private String query;
    private Long companyId;
    private Boolean partnerOnly;

    @Min(0)
    @Builder.Default
    private Integer page = 0;

    @Min(1)
    @Builder.Default
    private Integer size = 20;
}