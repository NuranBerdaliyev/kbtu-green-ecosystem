package com.example.green.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "vacancies",
        indexes = {
                @Index(name = "idx_vacancies_company", columnList = "company_id"),
                @Index(name = "idx_vacancies_active", columnList = "is_active")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vacancy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hr_manager_id", nullable = false)
    private User hrManager;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotBlank
    @Size(max = 255, message = "Title size is too big")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @NotBlank
    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
