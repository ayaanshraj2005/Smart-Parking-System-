package com.parknow.entity;

import com.parknow.entity.enums.VehicleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pricing_rules", indexes = {
    @Index(name = "idx_pricing_lot_vehicle", columnList = "lot_id, vehicle_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lot_id", nullable = false)
    private ParkingLot lot;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 30)
    private VehicleType vehicleType;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "base_hourly_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseHourlyRate;

    @NotNull
    @DecimalMin(value = "1.0")
    @Column(name = "peak_hour_multiplier", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal peakHourMultiplier = BigDecimal.valueOf(1.00);

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "overstay_penalty_rate", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal overstayPenaltyRate = BigDecimal.valueOf(20.00);
}
