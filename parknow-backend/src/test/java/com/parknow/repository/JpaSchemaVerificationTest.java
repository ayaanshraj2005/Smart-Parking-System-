package com.parknow.repository;

import com.parknow.entity.*;
import com.parknow.entity.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class JpaSchemaVerificationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ParkingLotRepository parkingLotRepository;

    @Autowired
    private ParkingSlotRepository parkingSlotRepository;

    @Autowired
    private PricingRuleRepository pricingRuleRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ParkingSessionRepository parkingSessionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("Verify complete entity relational graph creation and foreign key constraints")
    void testEntityRelationalGraph() {
        // 1. Roles & User
        Role role = roleRepository.save(Role.builder().name(RoleName.ROLE_USER).build());
        User user = userRepository.save(User.builder()
                .email("user@parknow.com")
                .passwordHash("hashedpassword")
                .fullName("John Doe")
                .phoneNumber("+1-555-0199")
                .roles(Set.of(role))
                .build());

        assertNotNull(user.getId());
        assertEquals("user@parknow.com", user.getEmail());

        // 2. Vehicle
        Vehicle vehicle = vehicleRepository.save(Vehicle.builder()
                .user(user)
                .licensePlate("ABC-1234")
                .vehicleType(VehicleType.CAR)
                .build());

        assertNotNull(vehicle.getId());

        // 3. ParkingLot
        ParkingLot lot = parkingLotRepository.save(ParkingLot.builder()
                .name("Central Mall Parking")
                .address("100 Main St")
                .city("Metropolis")
                .totalCapacity(50)
                .availableCapacity(50)
                .isActive(true)
                .build());

        // 4. ParkingSlot
        ParkingSlot slot = parkingSlotRepository.save(ParkingSlot.builder()
                .lot(lot)
                .slotNumber("A-101")
                .floorNumber(1)
                .vehicleType(VehicleType.CAR)
                .status(SlotStatus.AVAILABLE)
                .build());

        // 5. PricingRule
        PricingRule pricingRule = pricingRuleRepository.save(PricingRule.builder()
                .lot(lot)
                .vehicleType(VehicleType.CAR)
                .baseHourlyRate(BigDecimal.valueOf(25.00))
                .peakHourMultiplier(BigDecimal.valueOf(1.20))
                .overstayPenaltyRate(BigDecimal.valueOf(15.00))
                .build());

        // 6. Reservation
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = reservationRepository.save(Reservation.builder()
                .ticketCode("TICKET-999")
                .user(user)
                .slot(slot)
                .vehicle(vehicle)
                .startTime(now)
                .endTime(now.plusHours(2))
                .status(ReservationStatus.CONFIRMED)
                .estimatedAmount(BigDecimal.valueOf(50.00))
                .build());

        // 7. ParkingSession
        ParkingSession session = parkingSessionRepository.save(ParkingSession.builder()
                .reservation(reservation)
                .slot(slot)
                .vehicle(vehicle)
                .entryTime(now)
                .status(SessionStatus.IN_PROGRESS)
                .build());

        // 8. Payment
        Payment payment = paymentRepository.save(Payment.builder()
                .reservation(reservation)
                .session(session)
                .user(user)
                .transactionId("TXN-001122")
                .amount(BigDecimal.valueOf(50.00))
                .paymentStatus(PaymentStatus.COMPLETED)
                .paymentMethod("CREDIT_CARD")
                .build());

        assertNotNull(payment.getId());
        assertEquals("TXN-001122", payment.getTransactionId());
        assertEquals(user.getId(), payment.getUser().getId());
    }
}
