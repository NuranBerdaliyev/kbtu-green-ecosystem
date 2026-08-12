package com.example.green.api.mapper;

import com.example.green.api.dto.request.VacancyRequestDto;
import com.example.green.api.dto.response.VacancyResponseDto;
import com.example.green.domain.entity.User;
import com.example.green.domain.entity.Vacancy;
import org.springframework.stereotype.Component;

@Component
public class VacancyMapper {
    public Vacancy toEntity(VacancyRequestDto dto, User hrManager) {
        return Vacancy.builder()
                .hrManager(hrManager)
                .companyName(dto.getCompanyName())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .isPartnerVacancy(dto.getIsPartnerVacancy())
                .build();
    }

    public void updateEntity(Vacancy entity, VacancyRequestDto dto, User hrManager) {
        entity.setHrManager(hrManager);
        entity.setCompanyName(dto.getCompanyName());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setIsPartnerVacancy(dto.getIsPartnerVacancy());
    }

    public VacancyResponseDto toDto(Vacancy entity) {
        return VacancyResponseDto.builder()
                .id(entity.getId())
                .hrManagerId(entity.getHrManager().getId())
                .companyName(entity.getCompanyName())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .isPartnerVacancy(entity.getIsPartnerVacancy())
                .build();
    }
}
