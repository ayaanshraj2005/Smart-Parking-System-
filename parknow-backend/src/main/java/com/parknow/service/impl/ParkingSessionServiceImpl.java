package com.parknow.service.impl;

import com.parknow.dto.request.SessionEntryRequest;
import com.parknow.dto.request.SessionExitRequest;
import com.parknow.dto.response.ParkingSessionResponse;
import com.parknow.dto.response.PaymentResponse;
import com.parknow.entity.*;
import com.parknow.entity.enums.ReservationStatus;
import com.parknow.entity.enums.SessionStatus;
import com.parknow.entity.enums.SlotStatus;
import com.parknow.exception.InvalidOperationException;
import com.parknow.exception.ResourceNotFoundException;
import com.parknow.exception.UnauthorizedException;
import com.parknow.repository.*;
import com.parknow.service.BillingService;
import com.parknow.service.ParkingSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParkingSessionServiceImpl implements ParkingSessionService {

    private final ParkingSessionRepository parkingSessionRepository;
    private final ReservationRepository reservationRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final PaymentRepository paymentRepository;
    private final BillingService billingService;

    @Override
    @Transactional
    public ParkingSessionResponse startSession(Long userId, SessionEntryRequest request) {
        if (request == null || request.getTicketCode() == null || request.getTicketCode().isBlank()) {
            throw new InvalidOperationException("Ticket code is required for session entry.");
        }

        Reservation reservation = reservationRepository.findByTicketCode(request.getTicketCode())
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ticket code: " + request.getTicketCode()));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Reservation ticket does not belong to the authenticated user.");
        }

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new InvalidOperationException("Reservation cannot be checked-in. Current status: " + reservation.getStatus());
        }

        ParkingSlot slot = reservation.getSlot();
        if (slot.getStatus() == SlotStatus.MAINTENANCE || slot.getStatus() == SlotStatus.DISABLED) {
            throw new InvalidOperationException("Slot is under maintenance or disabled. Entry forbidden.");
        }
        if (slot.getStatus() == SlotStatus.OCCUPIED) {
            throw new InvalidOperationException("Slot state mismatch: Parking slot is already occupied.");
        }

        if (parkingSessionRepository.findByReservationId(reservation.getId()).isPresent()) {
            throw new InvalidOperationException("An active or completed parking session already exists for this reservation.");
        }

        LocalDateTime entryTime = (request.getEntryTime() != null) ? request.getEntryTime() : LocalDateTime.now();

        ParkingSession session = ParkingSession.builder()
                .reservation(reservation)
                .slot(slot)
                .vehicle(reservation.getVehicle())
                .entryTime(entryTime)
                .status(SessionStatus.IN_PROGRESS)
                .build();

        ParkingSession savedSession = parkingSessionRepository.save(session);

        reservation.setStatus(ReservationStatus.ACTIVE);
        reservationRepository.save(reservation);

        slot.setStatus(SlotStatus.OCCUPIED);
        parkingSlotRepository.save(slot);

        return mapToSessionResponse(savedSession, null, null);
    }

    @Override
    @Transactional
    public ParkingSessionResponse endSession(Long userId, Long sessionId, SessionExitRequest request, boolean isAdmin) {
        ParkingSession session = parkingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking session not found with id: " + sessionId));

        if (!isAdmin && !session.getReservation().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to check out this parking session.");
        }

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new InvalidOperationException("Parking session is already completed.");
        }

        LocalDateTime exitTime = (request != null && request.getExitTime() != null)
                ? request.getExitTime() : LocalDateTime.now();

        BillingService.BillingBreakdown breakdown = billingService.calculateBillingFee(session, exitTime);

        session.setExitTime(exitTime);
        session.setTotalFee(breakdown.getTotalFee());
        session.setStatus(SessionStatus.COMPLETED);

        ParkingSession savedSession = parkingSessionRepository.save(session);

        // Process Payment Record
        String paymentMethod = (request != null && request.getPaymentMethod() != null)
                ? request.getPaymentMethod() : "DIGITAL_WALLET";
        Payment payment = billingService.processPayment(savedSession, breakdown.getTotalFee(), paymentMethod);

        // Release slot & update lot capacity
        Reservation reservation = savedSession.getReservation();
        reservation.setStatus(ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);

        ParkingSlot slot = savedSession.getSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        parkingSlotRepository.save(slot);

        ParkingLot lot = slot.getLot();
        if (lot.getAvailableCapacity() < lot.getTotalCapacity()) {
            lot.setAvailableCapacity(lot.getAvailableCapacity() + 1);
            parkingLotRepository.save(lot);
        }

        return mapToSessionResponse(savedSession, breakdown, payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingSessionResponse> getActiveSessions(Long userId, boolean isAdmin) {
        List<ParkingSession> sessions = isAdmin
                ? parkingSessionRepository.findByStatus(SessionStatus.IN_PROGRESS)
                : parkingSessionRepository.findByReservationUserIdAndStatus(userId, SessionStatus.IN_PROGRESS);

        return sessions.stream()
                .map(s -> mapToSessionResponse(s, null, paymentRepository.findBySessionId(s.getId()).orElse(null)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingSessionResponse> getSessionHistory(Long userId, boolean isAdmin) {
        List<ParkingSession> sessions = isAdmin
                ? parkingSessionRepository.findAllByOrderByEntryTimeDesc()
                : parkingSessionRepository.findByReservationUserIdOrderByEntryTimeDesc(userId);

        return sessions.stream()
                .map(s -> mapToSessionResponse(s, null, paymentRepository.findBySessionId(s.getId()).orElse(null)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ParkingSessionResponse getSessionById(Long userId, Long sessionId, boolean isAdmin) {
        ParkingSession session = parkingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking session not found with id: " + sessionId));

        if (!isAdmin && !session.getReservation().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view this parking session.");
        }

        Payment payment = paymentRepository.findBySessionId(session.getId()).orElse(null);
        return mapToSessionResponse(session, null, payment);
    }

    private ParkingSessionResponse mapToSessionResponse(ParkingSession s, BillingService.BillingBreakdown breakdown, Payment payment) {
        PaymentResponse paymentResp = null;
        if (payment != null) {
            paymentResp = PaymentResponse.builder()
                    .id(payment.getId())
                    .transactionId(payment.getTransactionId())
                    .amount(payment.getAmount())
                    .paymentStatus(payment.getPaymentStatus())
                    .paymentMethod(payment.getPaymentMethod())
                    .paymentTime(payment.getPaymentTime())
                    .build();
        }

        BillingService.BillingBreakdown b = breakdown;
        if (b == null && s.getExitTime() != null) {
            b = billingService.calculateBillingFee(s, s.getExitTime());
        }

        return ParkingSessionResponse.builder()
                .id(s.getId())
                .reservationId(s.getReservation().getId())
                .ticketCode(s.getReservation().getTicketCode())
                .lotId(s.getSlot().getLot().getId())
                .lotName(s.getSlot().getLot().getName())
                .slotId(s.getSlot().getId())
                .slotNumber(s.getSlot().getSlotNumber())
                .floorNumber(s.getSlot().getFloorNumber())
                .licensePlate(s.getVehicle().getLicensePlate())
                .vehicleType(s.getVehicle().getVehicleType())
                .entryTime(s.getEntryTime())
                .exitTime(s.getExitTime())
                .status(s.getStatus())
                .durationMinutes(b != null ? b.getDurationMinutes() : null)
                .baseFee(b != null ? b.getBaseFee() : null)
                .overstayFee(b != null ? b.getOverstayFee() : null)
                .totalFee(s.getTotalFee() != null ? s.getTotalFee() : (b != null ? b.getTotalFee() : null))
                .paymentDetails(paymentResp)
                .build();
    }
}
