package com.parknow.service;

import com.parknow.entity.ParkingLot;
import com.parknow.entity.ParkingSlot;
import com.parknow.entity.ParkingSession;
import com.parknow.entity.PricingRule;
import com.parknow.entity.Reservation;
import com.parknow.entity.Vehicle;
import com.parknow.entity.enums.VehicleType;
import com.parknow.repository.PaymentRepository;
import com.parknow.repository.PricingRuleRepository;
import com.parknow.service.impl.BillingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private PricingRuleRepository pricingRuleRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private BillingServiceImpl billingService;

    private ParkingLot testLot;
    private Vehicle carVehicle;
    private Vehicle evVehicle;
    private ParkingSlot carSlot;

    @BeforeEach
    void setUp() {
        testLot = ParkingLot.builder().id(1L).name("Test Garage").build();

        carVehicle = Vehicle.builder().id(10L).vehicleType(VehicleType.CAR).build();
        evVehicle = Vehicle.builder().id(11L).vehicleType(VehicleType.ELECTRIC_VEHICLE).build();

        carSlot = ParkingSlot.builder().id(100L).lot(testLot).vehicleType(VehicleType.CAR).build();
    }

    @Test
    @DisplayName("1. Fee calculation for 2 hours with configured base rate")
    void testStandardFeeCalculation() {
        LocalDateTime entry = LocalDateTime.of(2026, 9, 13, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 9, 13, 12, 0);
        LocalDateTime reservedEnd = LocalDateTime.of(2026, 9, 13, 12, 0);

        Reservation res = Reservation.builder().endTime(reservedEnd).build();
        ParkingSession session = ParkingSession.builder()
                .slot(carSlot)
                .vehicle(carVehicle)
                .entryTime(entry)
                .reservation(res)
                .build();

        PricingRule rule = PricingRule.builder()
                .baseHourlyRate(BigDecimal.valueOf(40.00))
                .peakHourMultiplier(BigDecimal.valueOf(1.00))
                .overstayPenaltyRate(BigDecimal.valueOf(15.00))
                .build();

        when(pricingRuleRepository.findByLotIdAndVehicleType(1L, VehicleType.CAR))
                .thenReturn(Optional.of(rule));

        BillingService.BillingBreakdown breakdown = billingService.calculateBillingFee(session, exit);

        assertEquals(120, breakdown.getDurationMinutes());
        assertEquals(new BigDecimal("80.00"), breakdown.getBaseFee());
        assertEquals(new BigDecimal("0.00"), breakdown.getOverstayFee());
        assertEquals(new BigDecimal("80.00"), breakdown.getTotalFee());
    }

    @Test
    @DisplayName("2. EV pricing configuration calculation")
    void testEVPricingCalculation() {
        LocalDateTime entry = LocalDateTime.of(2026, 9, 13, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 9, 13, 11, 30); // 1.5 hrs -> rounded up to 2 hrs
        LocalDateTime reservedEnd = LocalDateTime.of(2026, 9, 13, 12, 0);

        Reservation res = Reservation.builder().endTime(reservedEnd).build();
        ParkingSlot evSlot = ParkingSlot.builder().id(101L).lot(testLot).vehicleType(VehicleType.ELECTRIC_VEHICLE).build();

        ParkingSession session = ParkingSession.builder()
                .slot(evSlot)
                .vehicle(evVehicle)
                .entryTime(entry)
                .reservation(res)
                .build();

        PricingRule evRule = PricingRule.builder()
                .baseHourlyRate(BigDecimal.valueOf(60.00))
                .peakHourMultiplier(BigDecimal.valueOf(1.20)) // Peak surcharge
                .overstayPenaltyRate(BigDecimal.valueOf(25.00))
                .build();

        when(pricingRuleRepository.findByLotIdAndVehicleType(1L, VehicleType.ELECTRIC_VEHICLE))
                .thenReturn(Optional.of(evRule));

        BillingService.BillingBreakdown breakdown = billingService.calculateBillingFee(session, exit);

        // 2 hrs * 60.00 * 1.20 = 144.00
        assertEquals(new BigDecimal("144.00"), breakdown.getBaseFee());
        assertEquals(new BigDecimal("0.00"), breakdown.getOverstayFee());
        assertEquals(new BigDecimal("144.00"), breakdown.getTotalFee());
    }

    @Test
    @DisplayName("3. Late exit overstay penalty fee calculation")
    void testOverstayPenaltyCalculation() {
        LocalDateTime entry = LocalDateTime.of(2026, 9, 13, 10, 0);
        LocalDateTime reservedEnd = LocalDateTime.of(2026, 9, 13, 12, 0); // Reserved 2 hrs
        LocalDateTime exit = LocalDateTime.of(2026, 9, 13, 13, 30);     // Exited 1.5 hrs late -> 2 overstay hrs

        Reservation res = Reservation.builder().endTime(reservedEnd).build();
        ParkingSession session = ParkingSession.builder()
                .slot(carSlot)
                .vehicle(carVehicle)
                .entryTime(entry)
                .reservation(res)
                .build();

        PricingRule rule = PricingRule.builder()
                .baseHourlyRate(BigDecimal.valueOf(50.00))
                .peakHourMultiplier(BigDecimal.valueOf(1.00))
                .overstayPenaltyRate(BigDecimal.valueOf(30.00))
                .build();

        when(pricingRuleRepository.findByLotIdAndVehicleType(1L, VehicleType.CAR))
                .thenReturn(Optional.of(rule));

        BillingService.BillingBreakdown breakdown = billingService.calculateBillingFee(session, exit);

        // Total duration: 3.5 hrs -> rounded up to 4 hrs = 4 * 50.00 = 200.00
        // Overstay: 1.5 hrs -> 2 hrs * 30.00 penalty = 60.00
        assertEquals(new BigDecimal("200.00"), breakdown.getBaseFee());
        assertEquals(new BigDecimal("60.00"), breakdown.getOverstayFee());
        assertEquals(new BigDecimal("260.00"), breakdown.getTotalFee());
    }

    @Test
    @DisplayName("4. Early exit calculation")
    void testEarlyExitCalculation() {
        LocalDateTime entry = LocalDateTime.of(2026, 9, 13, 10, 0);
        LocalDateTime reservedEnd = LocalDateTime.of(2026, 9, 13, 14, 0); // Reserved 4 hrs
        LocalDateTime exit = LocalDateTime.of(2026, 9, 13, 11, 15);     // Exited after 1 hr 15 mins -> 2 hrs

        Reservation res = Reservation.builder().endTime(reservedEnd).build();
        ParkingSession session = ParkingSession.builder()
                .slot(carSlot)
                .vehicle(carVehicle)
                .entryTime(entry)
                .reservation(res)
                .build();

        PricingRule rule = PricingRule.builder()
                .baseHourlyRate(BigDecimal.valueOf(40.00))
                .peakHourMultiplier(BigDecimal.valueOf(1.00))
                .overstayPenaltyRate(BigDecimal.valueOf(20.00))
                .build();

        when(pricingRuleRepository.findByLotIdAndVehicleType(1L, VehicleType.CAR))
                .thenReturn(Optional.of(rule));

        BillingService.BillingBreakdown breakdown = billingService.calculateBillingFee(session, exit);

        assertEquals(new BigDecimal("80.00"), breakdown.getBaseFee());
        assertEquals(new BigDecimal("0.00"), breakdown.getOverstayFee());
        assertEquals(new BigDecimal("80.00"), breakdown.getTotalFee());
    }
}
