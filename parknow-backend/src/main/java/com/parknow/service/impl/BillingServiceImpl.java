package com.parknow.service.impl;

import com.parknow.entity.ParkingSession;
import com.parknow.entity.Payment;
import com.parknow.entity.PricingRule;
import com.parknow.entity.enums.PaymentStatus;
import com.parknow.entity.enums.VehicleType;
import com.parknow.repository.PaymentRepository;
import com.parknow.repository.PricingRuleRepository;
import com.parknow.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final PricingRuleRepository pricingRuleRepository;
    private final PaymentRepository paymentRepository;

    private static final BigDecimal DEFAULT_BASE_RATE = BigDecimal.valueOf(50.00);
    private static final BigDecimal DEFAULT_OVERSTAY_PENALTY_RATE = BigDecimal.valueOf(20.00);

    @Override
    public BillingBreakdown calculateBillingFee(ParkingSession session, LocalDateTime exitTime) {
        if (session == null || session.getEntryTime() == null) {
            return new BillingBreakdown(0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        LocalDateTime effectiveExit = (exitTime != null && !exitTime.isBefore(session.getEntryTime()))
                ? exitTime : LocalDateTime.now();

        long minutes = Duration.between(session.getEntryTime(), effectiveExit).toMinutes();
        long durationHours = Math.max(1, (long) Math.ceil(minutes / 60.0));

        Long lotId = session.getSlot().getLot().getId();
        VehicleType vehicleType = session.getVehicle().getVehicleType();

        Optional<PricingRule> ruleOpt = pricingRuleRepository.findByLotIdAndVehicleType(lotId, vehicleType);

        BigDecimal baseRate = ruleOpt.map(PricingRule::getBaseHourlyRate).orElse(DEFAULT_BASE_RATE);
        BigDecimal multiplier = ruleOpt.map(PricingRule::getPeakHourMultiplier).orElse(BigDecimal.valueOf(1.00));
        BigDecimal penaltyRate = ruleOpt.map(PricingRule::getOverstayPenaltyRate).orElse(DEFAULT_OVERSTAY_PENALTY_RATE);

        // Base Fee calculation
        BigDecimal baseFee = baseRate.multiply(BigDecimal.valueOf(durationHours))
                .multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);

        // Overstay Fee calculation
        BigDecimal overstayFee = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (session.getReservation() != null && session.getReservation().getEndTime() != null) {
            LocalDateTime reservedEndTime = session.getReservation().getEndTime();
            if (effectiveExit.isAfter(reservedEndTime)) {
                long overstayMinutes = Duration.between(reservedEndTime, effectiveExit).toMinutes();
                long overstayHours = Math.max(1, (long) Math.ceil(overstayMinutes / 60.0));
                overstayFee = penaltyRate.multiply(BigDecimal.valueOf(overstayHours))
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }

        BigDecimal totalFee = baseFee.add(overstayFee);

        return BillingBreakdown.builder()
                .durationMinutes(minutes)
                .baseFee(baseFee)
                .overstayFee(overstayFee)
                .totalFee(totalFee)
                .build();
    }

    @Override
    @Transactional
    public Payment processPayment(ParkingSession session, BigDecimal amount, String paymentMethod) {
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String method = (paymentMethod != null && !paymentMethod.isBlank()) ? paymentMethod : "DIGITAL_WALLET";

        Payment payment = Payment.builder()
                .reservation(session.getReservation())
                .session(session)
                .user(session.getReservation().getUser())
                .transactionId(transactionId)
                .amount(amount)
                .paymentStatus(PaymentStatus.COMPLETED)
                .paymentMethod(method)
                .build();

        return paymentRepository.save(payment);
    }
}
