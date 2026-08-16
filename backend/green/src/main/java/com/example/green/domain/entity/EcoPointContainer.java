package com.example.green.domain.entity;

import com.example.green.domain.enums.WasteType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "eco_point_containers", indexes = {
        @Index(name = "idx_container_waste_type", columnList = "waste_type")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EcoPointContainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название контейнера обязательно")
    @Size(max = 255, message = "Название слишком длинное")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @NotNull(message = "Локация обязательна")
    @Column(name = "location", nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point location;

    @NotNull(message = "Тип отхода обязателен")
    @Enumerated(EnumType.STRING)
    @Column(name = "waste_type", nullable = false, length = 20)
    private WasteType wasteType;

    @NotNull(message = "fullnessPercentage обязателен")
    @Min(value = 0, message = "fullnessPercentage не может быть меньше 0")
    @Max(value = 100, message = "fullnessPercentage не может быть больше 100")
    @Column(name = "fullness_percentage", nullable = false)
    private Integer fullnessPercentage;

    @NotNull(message = "isActive обязателен")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @NotBlank(message = "QR token обязателен")
    @Size(max = 255, message = "QR token слишком длинный")
    @Column(name = "qr_code_token", nullable = false, unique = true, length = 255)
    private String qrCodeToken;
}
