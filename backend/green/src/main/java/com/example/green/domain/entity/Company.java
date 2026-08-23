package com.example.green.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "companies",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_company_hr_name", columnNames = {"hr_manager_id", "name"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hr_manager_id", nullable = false)
    private User hrManager;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Size(max = 500)
    @Column(length = 500)
    private String website;

    @NotNull
    @Column(name = "is_partner", nullable = false)
    private Boolean isPartner;
}
