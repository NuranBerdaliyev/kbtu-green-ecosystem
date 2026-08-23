package com.example.green.api.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyResponseDto {

    private Long id;
    private Long hrManagerId;
    private String name;
    private String description;
    private String website;
    private Boolean isPartner;
}