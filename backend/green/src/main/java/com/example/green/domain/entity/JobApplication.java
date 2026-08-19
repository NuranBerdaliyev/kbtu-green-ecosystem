package com.example.green.domain.entity;

import com.example.green.domain.enums.JobStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "job_applications",
        indexes = {
        @Index(name = "idx_job_app_status", columnList = "job_status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_job_application_vacancy_student", columnNames = {"vacancy_id", "student_id"})
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vacancy_id", nullable = false)
    private Vacancy vacancy;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @NotNull
    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @NotBlank
    @Size(min = 10, max = 5000, message = "Cover letter has to be between 10 and 5000 chars")
    @Column(name = "cover_letter", nullable = false, columnDefinition = "text")
    private String coverLetter;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "job_status", nullable = false, length = 20)
    private JobStatus jobStatus;

    public void changeStatus(JobStatus next) {
        if (next == null) {
            throw new IllegalArgumentException("Job status cannot be null");
        }

        if (jobStatus == next) {
            return;
        }

        boolean allowed = switch (jobStatus) {
            case PENDING ->
                    next == JobStatus.REVIEWED;

            case REVIEWED ->
                    next == JobStatus.ACCEPTED
                            || next == JobStatus.REJECTED;

            case ACCEPTED, REJECTED -> false;
        };

        if (!allowed) {
            throw new IllegalStateException(
                    "Invalid job application status transition: "
                            + jobStatus + " -> " + next
            );
        }

        this.jobStatus = next;
    }
}
