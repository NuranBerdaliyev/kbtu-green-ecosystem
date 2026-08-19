package com.example.green.api.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VacancyResponseDto {
    private Long id;
    private Long companyId;
    private String companyName;
    private Long hrManagerId;
    private String title;
    private String description;
    private Boolean partnerCompany;
}