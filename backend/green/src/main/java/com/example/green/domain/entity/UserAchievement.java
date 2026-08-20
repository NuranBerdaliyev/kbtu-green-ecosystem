package com.example.green.domain.entity;

import com.example.green.domain.enums.AchievementCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_achievement",
                        columnNames = {
                                "user_id",
                                "achievement_code"
                        }
                )
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "achievement_code", nullable = false, length = 50)
    private AchievementCode code;

    @NotNull
    @Column(name = "unlocked_at", nullable = false)
    private LocalDateTime unlockedAt;
}