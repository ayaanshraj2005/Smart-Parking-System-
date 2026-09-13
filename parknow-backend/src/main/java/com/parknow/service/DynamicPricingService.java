package com.parknow.service;

import com.parknow.entity.enums.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface DynamicPricingService {
    BigDecimal calculateEstimatedFee(Long lotId, VehicleType vehicleType, LocalDateTime startTime, LocalDateTime endTime);
}
