package com.example.green.domain.entity;
import com.example.green.domain.enums.WasteType;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "eco_point_containers", indexes = {
        @Index(name = "idx_container_waste_type", columnList = "wasteType")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder

public class EcoPointContainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WasteType wasteType;

    @Column(nullable = false)
    private Integer fullnessPercentage;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false, unique = true, length = 255)
    private String qrCodeToken;
}
