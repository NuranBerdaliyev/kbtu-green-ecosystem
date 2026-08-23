package com.example.green.service;

import com.example.green.api.dto.request.VacancyStatusRequestDto;
import com.example.green.api.dto.response.AdminDashboardResponseDto;
import com.example.green.api.dto.response.VacancyResponseDto;
import com.example.green.api.error.ForbiddenException;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.VacancyMapper;
import com.example.green.domain.entity.User;
import com.example.green.domain.entity.Vacancy;
import com.example.green.domain.enums.Role;
import com.example.green.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final VacancyRepository vacancyRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final EcoPointContainerRepository containerRepository;
    private final EcoTransactionRepository ecoTransactionRepository;
    private final CurrentUserService currentUserService;
    private final VacancyMapper vacancyMapper;

    @Transactional(readOnly = true)
    public AdminDashboardResponseDto getDashboard() {
        requireAdmin();

        return AdminDashboardResponseDto.builder()
                .totalUsers(userRepository.count())
                .students(userRepository.countByRole(Role.STUDENT))
                .employees(userRepository.countByRole(Role.EMPLOYEE))
                .hrManagers(userRepository.countByRole(Role.HR))
                .totalContainers(containerRepository.count())
                .activeContainers(
                        containerRepository.countByIsActiveTrue()
                )
                .criticalContainers(
                        containerRepository.countByIsActiveTrueAndFullnessPercentageGreaterThanEqual(90)
                )
                .totalCompanies(companyRepository.count())
                .partnerCompanies(companyRepository.countByIsPartnerTrue())
                .totalVacancies(vacancyRepository.count())
                .activeVacancies(vacancyRepository.countByIsActiveTrue())
                .totalApplications(jobApplicationRepository.count())
                .totalEcoTransactions(ecoTransactionRepository.count())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<VacancyResponseDto> getAllVacancies(int page, int size) {
        requireAdmin();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return vacancyRepository.findAll(pageable).map(vacancyMapper::toDto);
    }

    @Transactional
    public VacancyResponseDto changeVacancyStatus(Long vacancyId, VacancyStatusRequestDto request) {
        requireAdmin();
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vacancy not found: id=" + vacancyId)
                );

        if (Boolean.TRUE.equals(request.getIsActive())
                && !Boolean.TRUE.equals(vacancy.getCompany().getIsPartner())) {
            throw new IllegalStateException("Vacancy cannot be activated because company is not a partner");
        }

        vacancy.setIsActive(request.getIsActive());

        return vacancyMapper.toDto(vacancyRepository.save(vacancy));
    }

    private User requireAdmin() {
        User user = currentUserService.getCurrentUserOrThrow();

        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only administrators can perform this action");
        }

        return user;
    }
}