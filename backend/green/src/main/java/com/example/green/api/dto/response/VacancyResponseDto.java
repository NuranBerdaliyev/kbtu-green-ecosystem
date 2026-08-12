package com.example.green.api.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VacancyResponseDto {
    private Long id;
    private Long hrManagerId;
    private String companyName;
    private String title;
    private String description;
    private Boolean isPartnerVacancy;
}
