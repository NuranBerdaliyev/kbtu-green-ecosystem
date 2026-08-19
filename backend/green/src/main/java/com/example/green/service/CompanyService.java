package com.example.green.service;

import com.example.green.api.dto.request.CompanyRequestDto;
import com.example.green.api.dto.response.CompanyResponseDto;
import com.example.green.api.error.ForbiddenException;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.CompanyMapper;
import com.example.green.domain.entity.Company;
import com.example.green.domain.entity.User;
import com.example.green.domain.enums.Role;
import com.example.green.domain.repository.CompanyRepository;
import com.example.green.domain.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final VacancyRepository vacancyRepository;
    private final CompanyMapper companyMapper;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<CompanyResponseDto> findAll() {
        return companyRepository.findAll()
                .stream()
                .map(companyMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponseDto findById(Long id) {
        return companyMapper.toDto(
                getCompanyOrThrow(id)
        );
    }

    @Transactional(readOnly = true)
    public List<CompanyResponseDto> findMyCompanies() {
        User hr = requireHr();
        return companyRepository
                .findByHrManagerIdOrderByNameAsc(hr.getId())
                .stream()
                .map(companyMapper::toDto)
                .toList();
    }

    @Transactional
    public CompanyResponseDto create(CompanyRequestDto request) {
        User hr = requireHr();
        if (companyRepository.existsByHrManagerIdAndNameIgnoreCase(hr.getId(), request.getName())) {
            throw new IllegalStateException("Company with this name already exists");
        }
        Company company = companyMapper.toEntity(request, hr);
        return companyMapper.toDto(companyRepository.save(company));
    }

    @Transactional
    public CompanyResponseDto update(Long id, CompanyRequestDto request) {
        Company company = getCompanyOrThrow(id);
        requireCompanyOwner(company);
        companyMapper.updateEntity(company, request);

        return companyMapper.toDto(companyRepository.save(company));
    }

    @Transactional
    public void delete(Long id) {
        Company company = getCompanyOrThrow(id);
        requireCompanyOwner(company);
        if (vacancyRepository.existsByCompanyId(id)) {
            throw new IllegalStateException("Company with vacancies cannot be deleted");
        }
        companyRepository.delete(company);
    }

    private Company getCompanyOrThrow(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: id=" + id));
    }

    private User requireHr() {
        User user = currentUserService.getCurrentUserOrThrow();
        if (user.getRole() != Role.HR) {
            throw new ForbiddenException("Only HR users can perform this action");
        }
        return user;
    }

    private void requireCompanyOwner(Company company) {
        User current = requireHr();
        if (!company.getHrManager()
                .getId()
                .equals(current.getId())) {

            throw new ForbiddenException("Only company owner can perform this action");
        }
    }
}