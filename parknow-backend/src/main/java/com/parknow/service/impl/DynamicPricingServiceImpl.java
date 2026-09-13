package com.parknow.service.impl;

import com.parknow.entity.PricingRule;
import com.parknow.entity.enums.VehicleType;
import com.parknow.repository.PricingRuleRepository;
import com.parknow.service.DynamicPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DynamicPricingServiceImpl implements DynamicPricingService {

    private final PricingRuleRepository pricingRuleRepository;
    private static final BigDecimal DEFAULT_HOURLY_RATE = BigDecimal.valueOf(50.00);

    @Override
    public BigDecimal calculateEstimatedFee(Long lotId, VehicleType vehicleType, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            return BigDecimal.ZERO;
        }

        long minutes = Duration.between(startTime, endTime).toMinutes();
        double hours = Math.ceil(minutes / 60.0);
        long durationHours = Math.max(1, (long) hours);

        Optional<PricingRule> pricingRuleOpt = pricingRuleRepository.findByLotIdAndVehicleType(lotId, vehicleType);

        BigDecimal hourlyRate = pricingRuleOpt.map(PricingRule::getBaseHourlyRate).orElse(DEFAULT_HOURLY_RATE);
        BigDecimal multiplier = pricingRuleOpt.map(PricingRule::getPeakHourMultiplier).orElse(BigDecimal.valueOf(1.00));

        return hourlyRate.multiply(BigDecimal.valueOf(durationHours))
                .multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
