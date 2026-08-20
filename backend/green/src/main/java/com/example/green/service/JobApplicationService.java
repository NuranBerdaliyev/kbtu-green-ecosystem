package com.example.green.service;

import com.example.green.api.dto.request.JobApplicationRequestDto;
import com.example.green.api.dto.request.JobApplicationStatusRequestDto;
import com.example.green.api.dto.response.CandidateResponseDto;
import com.example.green.api.dto.response.JobApplicationResponseDto;
import com.example.green.api.error.ForbiddenException;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.JobApplicationMapper;
import com.example.green.domain.entity.JobApplication;
import com.example.green.domain.entity.User;
import com.example.green.domain.entity.Vacancy;
import com.example.green.domain.enums.CandidateSort;
import com.example.green.domain.enums.Role;
import com.example.green.domain.repository.JobApplicationRepository;
import com.example.green.domain.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobApplicationService {
    private final JobApplicationRepository jobApplicationRepository;
    private final VacancyRepository vacancyRepository;
    private final JobApplicationMapper jobApplicationMapper;
    private final CurrentUserService currentUserService;
    private static final int RECOMMENDED_ESG_THRESHOLD = 70;

    @Transactional
    public JobApplicationResponseDto apply(Long vacancyId, JobApplicationRequestDto request) {
        User student = requireStudent();
        Vacancy vacancy = getVacancyOrThrow(vacancyId);
        if (jobApplicationRepository.existsByVacancyIdAndStudentId(vacancyId, student.getId())) {
            throw new IllegalStateException("Student already applied to this vacancy");
        }

        JobApplication application = jobApplicationMapper.toEntity(request.getCoverLetter(), vacancy, student);
        return jobApplicationMapper.toDto(jobApplicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponseDto> findMyApplications() {
        User student = requireStudent();
        return jobApplicationRepository
                .findByStudentIdOrderByAppliedAtDesc(
                        student.getId()
                )
                .stream()
                .map(jobApplicationMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CandidateResponseDto> findCandidates(Long vacancyId, CandidateSort sort) {
        Vacancy vacancy = getVacancyOrThrow(vacancyId);
        requireVacancyOwner(vacancy);
        CandidateSort effectiveSort = sort == null ? CandidateSort.ESG_DESC : sort;
        List<JobApplication> applications = switch (effectiveSort) {
            case ESG_DESC ->
                    jobApplicationRepository
                            .findByVacancyIdOrderByStudent_EsgRatingDescAppliedAtAsc(
                                    vacancyId
                            );

            case APPLIED_AT_DESC ->
                    jobApplicationRepository
                            .findByVacancyIdOrderByAppliedAtDesc(vacancyId);
        };
        return applications.stream()
                .map(application -> {
                    Integer esg = application
                            .getStudent()
                            .getEsgRating();

                    boolean recommended =
                            esg != null
                                    && esg >= RECOMMENDED_ESG_THRESHOLD;

                    return jobApplicationMapper.toCandidateDto(
                            application,
                            recommended
                    );
                })
                .toList();
    }

    @Transactional
    public CandidateResponseDto changeStatus(Long applicationId, JobApplicationStatusRequestDto request) {
        JobApplication application = getApplicationOrThrow(applicationId);
        requireVacancyOwner(application.getVacancy());
        application.changeStatus(request.getStatus());

        JobApplication saved = jobApplicationRepository.save(application);
        Integer esg = saved.getStudent().getEsgRating();
        boolean recommended = esg != null && esg >= RECOMMENDED_ESG_THRESHOLD;

        return jobApplicationMapper.toCandidateDto(saved, recommended);
    }

    private User requireStudent() {
        User user = currentUserService.getCurrentUserOrThrow();
        if (user.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Only students can apply for vacancies");
        }

        return user;
    }

    private User requireHr() {
        User user = currentUserService.getCurrentUserOrThrow();
        if (user.getRole() != Role.HR) {
            throw new ForbiddenException("Only HR users can perform this action");
        }

        return user;
    }

    private void requireVacancyOwner(Vacancy vacancy) {
        User hr = requireHr();

        if (!vacancy.getHrManager().getId().equals(hr.getId())) {
            throw new ForbiddenException("Only vacancy owner can access its applications");
        }
    }

    private Vacancy getVacancyOrThrow(Long id) {
        return vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found: id=" + id));
    }

    private JobApplication getApplicationOrThrow(Long id) {
        return jobApplicationRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("JobApplication not found: id=" + id)
                );
    }
}