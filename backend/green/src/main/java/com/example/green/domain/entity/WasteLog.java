package com.example.green.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "waste_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WasteLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eco_point_container_id", nullable = false)
    private EcoPointContainer ecoPointContainer;

    @Column(nullable = false)
    private LocalDateTime scannedAt;

    @Column(nullable = false)
    private Integer ecoCoinsEarned;
}
