package com.example.green.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "vacancies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vacancy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "HR manager обязателен")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hr_manager_id", nullable = false)
    private User hrManager;

    @NotBlank(message = "Название компании обязательно")
    @Size(max = 255, message = "Название компании слишком длинное")
    @Column(nullable = false, length = 255)
    private String companyName;

    @NotBlank(message = "Название вакансии обязательно")
    @Size(max = 255, message = "Название вакансии слишком длинное")
    @Column(nullable = false, length = 255)
    private String title;

    @NotBlank(message = "Описание обязательно")
    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @NotNull(message = "isPartnerVacancy обязателен")
    @Column(nullable = false)
    private Boolean isPartnerVacancy;
}
