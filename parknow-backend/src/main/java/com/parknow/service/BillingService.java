package com.parknow.service;

import com.parknow.entity.ParkingSession;
import com.parknow.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface BillingService {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    class BillingBreakdown {
        private long durationMinutes;
        private BigDecimal baseFee;
        private BigDecimal overstayFee;
        private BigDecimal totalFee;
    }

    BillingBreakdown calculateBillingFee(ParkingSession session, LocalDateTime exitTime);

    Payment processPayment(ParkingSession session, BigDecimal amount, String paymentMethod);
}
