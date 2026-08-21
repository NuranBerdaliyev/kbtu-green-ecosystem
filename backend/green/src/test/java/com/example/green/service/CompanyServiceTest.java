package com.example.green.service;

import com.example.green.api.dto.request.CompanyPartnerStatusRequestDto;
import com.example.green.api.dto.request.CompanyRequestDto;
import com.example.green.api.error.ForbiddenException;
import com.example.green.api.mapper.CompanyMapper;
import com.example.green.domain.entity.Company;
import com.example.green.domain.entity.User;
import com.example.green.domain.enums.Role;
import com.example.green.domain.repository.CompanyRepository;
import com.example.green.domain.repository.VacancyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private VacancyRepository vacancyRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Spy
    private CompanyMapper companyMapper = new CompanyMapper();

    @InjectMocks
    private CompanyService companyService;

    @Test
    void createShouldAlwaysCreateNonPartnerCompany() {
        User hr = user(1L, Role.HR);

        CompanyRequestDto request = CompanyRequestDto.builder()
                .name("Green Company")
                .description("Description")
                .website("https://example.com")
                .build();

        when(currentUserService.getCurrentUserOrThrow())
                .thenReturn(hr);

        when(companyRepository.existsByHrManagerIdAndNameIgnoreCase(
                1L,
                "Green Company"
        )).thenReturn(false);

        when(companyRepository.save(any(Company.class)))
                .thenAnswer(invocation -> {
                    Company company = invocation.getArgument(0);
                    company.setId(10L);
                    return company;
                });

        var response = companyService.create(request);

        assertFalse(response.getIsPartner());
        assertEquals(1L, response.getHrManagerId());
    }

    @Test
    void updateShouldPreservePartnerStatus() {
        User hr = user(1L, Role.HR);

        Company company = Company.builder()
                .id(10L)
                .hrManager(hr)
                .name("Old name")
                .isPartner(true)
                .build();

        CompanyRequestDto request = CompanyRequestDto.builder()
                .name("New name")
                .build();

        when(currentUserService.getCurrentUserOrThrow())
                .thenReturn(hr);

        when(companyRepository.findById(10L))
                .thenReturn(java.util.Optional.of(company));

        when(companyRepository.save(company))
                .thenReturn(company);

        var response = companyService.update(10L, request);

        assertEquals("New name", response.getName());
        assertTrue(response.getIsPartner());
    }

    @Test
    void hrShouldNotChangePartnerStatus() {
        User hr = user(1L, Role.HR);

        when(currentUserService.getCurrentUserOrThrow())
                .thenReturn(hr);

        var request =
                new CompanyPartnerStatusRequestDto(true);

        assertThrows(
                ForbiddenException.class,
                () -> companyService.changePartnerStatus(
                        10L,
                        request
                )
        );

        verify(companyRepository, never()).save(any());
    }

    @Test
    void adminShouldChangePartnerStatus() {
        User admin = user(1L, Role.ADMIN);
        User hr = user(2L, Role.HR);

        Company company = Company.builder()
                .id(10L)
                .hrManager(hr)
                .name("Company")
                .isPartner(false)
                .build();

        when(currentUserService.getCurrentUserOrThrow())
                .thenReturn(admin);

        when(companyRepository.findById(10L))
                .thenReturn(java.util.Optional.of(company));

        when(companyRepository.save(company))
                .thenReturn(company);

        var response = companyService.changePartnerStatus(
                10L,
                new CompanyPartnerStatusRequestDto(true)
        );

        assertTrue(response.getIsPartner());
    }

    private User user(Long id, Role role) {
        return User.builder()
                .id(id)
                .email("user" + id + "@test.com")
                .fullName("Test User")
                .role(role)
                .passwordHash("x".repeat(60))
                .ecoCoinsBalance(0L)
                .esgRating(0)
                .totalCo2Saved(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();
    }
}