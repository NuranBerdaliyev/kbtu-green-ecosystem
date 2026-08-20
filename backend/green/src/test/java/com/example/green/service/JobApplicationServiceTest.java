package com.example.green.service;

import com.example.green.api.mapper.JobApplicationMapper;
import com.example.green.domain.entity.JobApplication;
import com.example.green.domain.entity.User;
import com.example.green.domain.entity.Vacancy;
import com.example.green.domain.enums.CandidateSort;
import com.example.green.domain.enums.JobStatus;
import com.example.green.domain.enums.Role;
import com.example.green.domain.repository.JobApplicationRepository;
import com.example.green.domain.repository.VacancyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private VacancyRepository vacancyRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Spy
    private JobApplicationMapper jobApplicationMapper =
            new JobApplicationMapper();

    @InjectMocks
    private JobApplicationService jobApplicationService;

    @Test
    void shouldReturnCandidatesSortedByEsgAndMarkRecommended() {
        User hr = user(1L, Role.HR, 0);
        User strongStudent = user(2L, Role.STUDENT, 85);
        User regularStudent = user(3L, Role.STUDENT, 45);

        Vacancy vacancy = Vacancy.builder()
                .id(10L)
                .hrManager(hr)
                .title("Java Intern")
                .description("Description")
                .build();

        JobApplication strong =
                application(100L, vacancy, strongStudent);

        JobApplication regular =
                application(101L, vacancy, regularStudent);

        when(currentUserService.getCurrentUserOrThrow())
                .thenReturn(hr);

        when(vacancyRepository.findById(10L))
                .thenReturn(Optional.of(vacancy));

        when(jobApplicationRepository
                .findByVacancyIdOrderByStudent_EsgRatingDescAppliedAtAsc(10L))
                .thenReturn(List.of(strong, regular));

        var result = jobApplicationService.findCandidates(
                10L,
                CandidateSort.ESG_DESC
        );

        assertEquals(2, result.size());
        assertEquals(85, result.get(0).getEsgRating());
        assertTrue(result.get(0).getRecommended());
        assertFalse(result.get(1).getRecommended());

        verify(jobApplicationRepository)
                .findByVacancyIdOrderByStudent_EsgRatingDescAppliedAtAsc(10L);
    }

    @Test
    void shouldUseAppliedAtSorting() {
        User hr = user(1L, Role.HR, 0);

        Vacancy vacancy = Vacancy.builder()
                .id(10L)
                .hrManager(hr)
                .title("Java Intern")
                .description("Description")
                .build();

        when(currentUserService.getCurrentUserOrThrow())
                .thenReturn(hr);

        when(vacancyRepository.findById(10L))
                .thenReturn(Optional.of(vacancy));

        when(jobApplicationRepository
                .findByVacancyIdOrderByAppliedAtDesc(10L))
                .thenReturn(List.of());

        jobApplicationService.findCandidates(
                10L,
                CandidateSort.APPLIED_AT_DESC
        );

        verify(jobApplicationRepository)
                .findByVacancyIdOrderByAppliedAtDesc(10L);
    }

    private JobApplication application(
            Long id,
            Vacancy vacancy,
            User student
    ) {
        return JobApplication.builder()
                .id(id)
                .vacancy(vacancy)
                .student(student)
                .appliedAt(LocalDateTime.now())
                .coverLetter("Valid cover letter")
                .jobStatus(JobStatus.PENDING)
                .build();
    }

    private User user(Long id, Role role, int esg) {
        return User.builder()
                .id(id)
                .email("user" + id + "@test.com")
                .fullName("Test User " + id)
                .role(role)
                .passwordHash("x".repeat(60))
                .ecoCoinsBalance(100L)
                .esgRating(esg)
                .totalCo2Saved(BigDecimal.ONE)
                .createdAt(LocalDateTime.now())
                .build();
    }
}