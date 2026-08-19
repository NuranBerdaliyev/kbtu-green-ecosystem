package com.example.green.api.mapper;

import com.example.green.api.dto.request.CompanyRequestDto;
import com.example.green.api.dto.response.CompanyResponseDto;
import com.example.green.domain.entity.Company;
import com.example.green.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {
    public Company toEntity(CompanyRequestDto dto, User hrManager) {
        return Company.builder()
                .hrManager(hrManager)
                .name(dto.getName())
                .description(dto.getDescription())
                .website(dto.getWebsite())
                .isPartner(Boolean.TRUE.equals(dto.getIsPartner()))
                .build();
    }

    public void updateEntity(Company company, CompanyRequestDto dto) {
        company.setName(dto.getName());
        company.setDescription(dto.getDescription());
        company.setWebsite(dto.getWebsite());
        company.setIsPartner(
                Boolean.TRUE.equals(dto.getIsPartner())
        );
    }

    public CompanyResponseDto toDto(Company company) {
        return CompanyResponseDto.builder()
                .id(company.getId())
                .hrManagerId(company.getHrManager().getId())
                .name(company.getName())
                .description(company.getDescription())
                .website(company.getWebsite())
                .isPartner(company.getIsPartner())
                .build();
    }
}