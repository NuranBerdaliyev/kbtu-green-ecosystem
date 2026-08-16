package com.example.green.domain.entity;
import com.example.green.domain.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email(message = "Некорректный email")
    @NotBlank(message = "Email обязателен")
    @Size(max = 255, message = "Email слишком длинный")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank(message = "ФИО обязательно")
    @Size(max = 255, message = "ФИО слишком длинное")
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @NotNull(message = "Роль обязательна")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 60, max = 72, message = "Некорректная длина хеша пароля")
    @Column(name = "password_hash", nullable = false, length = 72)
    private String passwordHash;

    @NotNull(message = "Баланс ecoCoins обязателен")
    @Min(value = 0, message = "Баланс ecoCoins не может быть отрицательным")
    @Column(name = "eco_coins_balance", nullable = false)
    private Long ecoCoinsBalance;

    @NotNull(message = "ESG рейтинг обязателен")
    @Min(value = 0, message = "ESG рейтинг не может быть меньше 0")
    @Max(value = 100, message = "ESG рейтинг не может быть больше 100")
    @Column(name = "esg_rating", nullable = false)
    private Integer esgRating;

    @NotNull(message = "totalCo2Saved обязателен")
    @DecimalMin(value = "0.0", inclusive = true, message = "totalCo2Saved не может быть отрицательным")
    @Digits(integer = 12, fraction = 3, message = "Некорректный формат totalCo2Saved")
    @Column(name = "total_co2_saved", nullable = false, precision = 15, scale = 3)
    private BigDecimal totalCo2Saved;

    @NotNull(message = "createdAt обязателен")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}