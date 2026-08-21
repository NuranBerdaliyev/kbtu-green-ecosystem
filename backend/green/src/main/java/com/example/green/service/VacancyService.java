package com.example.green.service;

import com.example.green.api.dto.request.VacancyRequestDto;
import com.example.green.api.dto.request.VacancySearchRequestDto;
import com.example.green.api.dto.response.VacancyResponseDto;
import com.example.green.api.error.ForbiddenException;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.VacancyMapper;
import com.example.green.domain.entity.Company;
import com.example.green.domain.entity.User;
import com.example.green.domain.entity.Vacancy;
import com.example.green.domain.enums.Role;
import com.example.green.domain.repository.CompanyRepository;
import com.example.green.domain.repository.JobApplicationRepository;
import com.example.green.domain.repository.VacancyRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VacancyService {
    private final VacancyRepository vacancyRepository;
    private final CompanyRepository companyRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final VacancyMapper vacancyMapper;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Page<VacancyResponseDto> search(VacancySearchRequestDto request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.DESC, "id")
        );
        Specification<Vacancy> specification = buildSpecification(request);

        return vacancyRepository
                .findAll(specification, pageable)
                .map(vacancyMapper::toDto);
    }

    @Transactional(readOnly = true)
    public VacancyResponseDto findById(Long id) {
        Vacancy vacancy = vacancyRepository
                .findByIdAndIsActiveTrueAndCompany_IsPartnerTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Published vacancy not found: id=" + id));
        return vacancyMapper.toDto(vacancy);
    }

    @Transactional(readOnly = true)
    public List<VacancyResponseDto> findMyVacancies() {
        User hr = requireHr();
        return vacancyRepository
                .findByHrManagerIdOrderByIdDesc(
                        hr.getId()
                )
                .stream()
                .map(vacancyMapper::toDto)
                .toList();
    }

    @Transactional
    public VacancyResponseDto create(VacancyRequestDto request) {
        User hr = requireHr();
        Company company = getCompanyOrThrow(request.getCompanyId());
        requirePartnerCompany(company);
        requireCompanyOwner(company, hr);
        Vacancy vacancy = vacancyMapper.toEntity(request, hr, company);

        return vacancyMapper.toDto(vacancyRepository.save(vacancy));
    }

    @Transactional
    public VacancyResponseDto update(Long id, VacancyRequestDto request) {
        Vacancy vacancy = getVacancyOrThrow(id);
        User hr = requireVacancyOwner(vacancy);
        Company company = getCompanyOrThrow(request.getCompanyId());
        requirePartnerCompany(company);
        requireCompanyOwner(company, hr);
        vacancyMapper.updateEntity(vacancy, request, company);

        return vacancyMapper.toDto(vacancyRepository.save(vacancy));
    }

    @Transactional
    public void delete(Long id) {
        Vacancy vacancy = getVacancyOrThrow(id);
        requireVacancyOwner(vacancy);

        if (jobApplicationRepository.existsByVacancyId(id)) {
            throw new IllegalStateException("Vacancy with applications cannot be deleted");
        }
        vacancyRepository.delete(vacancy);
    }

    private Specification<Vacancy> buildSpecification(VacancySearchRequestDto request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("isActive")));
            predicates.add(cb.isTrue(root.get("company").get("isPartner")));

            if (request.getQuery() != null && !request.getQuery().isBlank()) {
                String pattern = "%" + request.getQuery().toLowerCase().trim() + "%";
                Predicate title = cb.like(cb.lower(root.get("title")), pattern);
                Predicate description = cb.like(cb.lower(root.get("description")), pattern);

                predicates.add(
                        cb.or(title, description)
                );
            }

            if (request.getCompanyId() != null) {
                predicates.add(cb.equal(root.get("company").get("id"), request.getCompanyId()));
            }

            if (Boolean.TRUE.equals(request.getPartnerOnly())) {
                predicates.add(cb.isTrue(root.get("company").get("isPartner")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Vacancy getVacancyOrThrow(Long id) {
        return vacancyRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vacancy not found: id=" + id)
                );
    }

    private Company getCompanyOrThrow(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Company not found: id=" + id)
                );
    }

    private User requireHr() {
        User user = currentUserService.getCurrentUserOrThrow();
        if (user.getRole() != Role.HR) {
            throw new ForbiddenException("Only HR users can perform this action");
        }
        return user;
    }

    private User requireVacancyOwner(Vacancy vacancy) {
        User hr = requireHr();
        if (!vacancy.getHrManager().getId().equals(hr.getId())) {
            throw new ForbiddenException("Only vacancy owner can perform this action");
        }
        return hr;
    }

    private void requireCompanyOwner(Company company, User hr) {
        if (!company.getHrManager().getId().equals(hr.getId())) {
            throw new ForbiddenException("HR cannot create vacancy for another company");
        }
    }

    private void requirePartnerCompany(Company company) {
        if (!Boolean.TRUE.equals(company.getIsPartner())) {
            throw new IllegalStateException("Only partner companies can publish vacancies");
        }
    }
}