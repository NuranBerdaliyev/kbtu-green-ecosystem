package com.example.green.domain.entity;

import com.example.green.domain.enums.JobStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "job_applications",
        indexes = {
        @Index(name = "idx_job_app_status", columnList = "jobStatus")
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

    @NotNull(message = "Vacancy обязателен")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vacancy_id", nullable = false)
    private Vacancy vacancy;

    @NotNull(message = "Student обязателен")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @NotNull(message = "Дата подачи обязательна")
    @Column(nullable = false)
    private LocalDateTime appliedAt;

    @NotBlank(message = "Cover letter обязателен")
    @Size(min = 10, max = 5000, message = "Cover letter должен быть от 10 до 5000 символов")
    @Column(nullable = false, columnDefinition = "text")
    private String coverLetter;

    @NotNull(message = "Статус заявки обязателен")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus jobStatus;
}
