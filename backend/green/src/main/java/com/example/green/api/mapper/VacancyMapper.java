package com.example.green.api.mapper;

import com.example.green.api.dto.request.VacancyRequestDto;
import com.example.green.api.dto.response.VacancyResponseDto;
import com.example.green.domain.entity.Company;
import com.example.green.domain.entity.User;
import com.example.green.domain.entity.Vacancy;
import org.springframework.stereotype.Component;

@Component
public class VacancyMapper {
    public Vacancy toEntity(VacancyRequestDto dto, User hrManager, Company company) {
        return Vacancy.builder()
                .hrManager(hrManager)
                .company(company)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .build();
    }

    public void updateEntity(Vacancy vacancy, VacancyRequestDto dto, Company company) {
        vacancy.setCompany(company);
        vacancy.setTitle(dto.getTitle());
        vacancy.setDescription(dto.getDescription());
    }

    public VacancyResponseDto toDto(Vacancy vacancy) {
        return VacancyResponseDto.builder()
                .id(vacancy.getId())
                .companyId(vacancy.getCompany().getId())
                .companyName(vacancy.getCompany().getName())
                .hrManagerId(vacancy.getHrManager().getId())
                .title(vacancy.getTitle())
                .description(vacancy.getDescription())
                .partnerCompany(
                        vacancy.getCompany().getIsPartner()
                )
                .build();
    }
}