package com.example.green.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyPartnerStatusRequestDto {

    @NotNull
    private Boolean isPartner;
}