package com.example.green.config;

import com.example.green.domain.enums.WasteType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.gamification")
public class GamificationProperties {
    @Valid
    @NotNull
    private Trip trip = new Trip();

    @Valid
    @NotNull
    private Waste waste = new Waste();

    @Min(0)
    @Max(100)
    private int recommendedEsgThreshold = 70;
    @Getter
    @Setter
    public static class Trip {

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal coinsPerKm = BigDecimal.valueOf(2);

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal co2KgPerPassengerKm =
                BigDecimal.valueOf(0.12);

        @Min(1)
        private int esgPerCompletedTrip = 2;
    }

    @Getter
    @Setter
    public static class Waste {

        @Min(1)
        private int gramsPerCoin = 100;

        @Min(1)
        private int esgPerDeposit = 1;

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal plasticCo2KgPerKg =
                BigDecimal.valueOf(1.5);
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal paperCo2KgPerKg =
                BigDecimal.valueOf(0.9);

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal glassCo2KgPerKg =
                BigDecimal.valueOf(0.3);

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal batteryCo2KgPerKg =
                BigDecimal.valueOf(2.0);

        public BigDecimal co2Coefficient(WasteType wasteType) {
            return switch (wasteType) {
                case PLASTIC -> plasticCo2KgPerKg;
                case PAPER -> paperCo2KgPerKg;
                case GLASS -> glassCo2KgPerKg;
                case BATTERY -> batteryCo2KgPerKg;
            };
        }
    }
}
