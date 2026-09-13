package com.parknow.service.impl;

import com.parknow.dto.request.CreateReservationRequest;
import com.parknow.dto.response.ReservationResponse;
import com.parknow.entity.ParkingLot;
import com.parknow.entity.ParkingSlot;
import com.parknow.entity.Reservation;
import com.parknow.entity.User;
import com.parknow.entity.Vehicle;
import com.parknow.entity.enums.ReservationStatus;
import com.parknow.entity.enums.SlotStatus;
import com.parknow.exception.DoubleBookingException;
import com.parknow.exception.InvalidOperationException;
import com.parknow.exception.ResourceNotFoundException;
import com.parknow.exception.SlotNotAvailableException;
import com.parknow.exception.UnauthorizedException;
import com.parknow.repository.ParkingLotRepository;
import com.parknow.repository.ParkingSlotRepository;
import com.parknow.repository.ReservationRepository;
import com.parknow.repository.UserRepository;
import com.parknow.repository.VehicleRepository;
import com.parknow.service.DynamicPricingService;
import com.parknow.service.ReservationService;
import com.parknow.util.SlotPriorityAllocator;
import com.parknow.util.TicketCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final DynamicPricingService dynamicPricingService;
    private final SlotPriorityAllocator slotPriorityAllocator;

    @Override
    @Transactional
    public ReservationResponse createReservation(Long userId, CreateReservationRequest request) {
        // 1. Time & Request Validation
        if (request == null) {
            throw new InvalidOperationException("Reservation request cannot be null.");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new InvalidOperationException("Start time and end time are required.");
        }
        if (request.getLotId() == null) {
            throw new InvalidOperationException("Parking lot ID is required.");
        }
        if (request.getVehicleId() == null) {
            throw new InvalidOperationException("Vehicle ID is required.");
        }
        if (request.getVehicleType() == null) {
            throw new InvalidOperationException("Vehicle type is required.");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new InvalidOperationException("Start time must be strictly before end time.");
        }
        if (request.getStartTime().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new InvalidOperationException("Start time cannot be in the past.");
        }

        // 2. User & Vehicle Validation
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + request.getVehicleId()));

        if (!vehicle.getUser().getId().equals(userId)) {
            throw new InvalidOperationException("Vehicle does not belong to the authenticated user.");
        }

        if (vehicle.getVehicleType() != request.getVehicleType()) {
            throw new InvalidOperationException("Requested vehicle type (" + request.getVehicleType() +
                    ") does not match vehicle's registered type (" + vehicle.getVehicleType() + ").");
        }

        // 3. Parking Lot Validation
        ParkingLot lot = parkingLotRepository.findById(request.getLotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + request.getLotId()));

        if (!lot.getIsActive()) {
            throw new InvalidOperationException("Selected parking lot is currently inactive.");
        }

        // 4. Find Candidate Slots & Intelligent Priority Allocation
        List<ParkingSlot> candidateSlots = parkingSlotRepository.findByLotIdAndVehicleTypeAndStatusNotIn(
                lot.getId(), request.getVehicleType(), List.of(SlotStatus.MAINTENANCE, SlotStatus.DISABLED));

        if (candidateSlots.isEmpty()) {
            throw new SlotNotAvailableException("No available slots found for lot " + lot.getName() +
                    " and vehicle type " + request.getVehicleType());
        }

        // Priority allocation loop using DSA min-heap priority allocator
        ParkingSlot selectedSlot = null;
        List<ParkingSlot> remainingCandidates = new ArrayList<>(candidateSlots);

        while (!remainingCandidates.isEmpty()) {
            Optional<ParkingSlot> bestCandidateOpt = slotPriorityAllocator.allocateBestSlot(remainingCandidates, request.getVehicleType());
            if (bestCandidateOpt.isEmpty()) {
                break;
            }

            ParkingSlot candidate = bestCandidateOpt.get();
            remainingCandidates.remove(candidate);

            // Pessimistic DB Lock on candidate slot to prevent double-booking race condition
            Optional<ParkingSlot> lockedSlotOpt = parkingSlotRepository.findByIdWithLock(candidate.getId());
            if (lockedSlotOpt.isEmpty()) {
                continue;
            }

            ParkingSlot lockedSlot = lockedSlotOpt.get();
            if (lockedSlot.getStatus() == SlotStatus.MAINTENANCE || lockedSlot.getStatus() == SlotStatus.DISABLED) {
                continue;
            }

            // Check DB for overlapping active/confirmed reservations
            List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                    lockedSlot.getId(), request.getStartTime(), request.getEndTime());

            if (conflicts.isEmpty()) {
                selectedSlot = lockedSlot;
                break;
            }
        }

        if (selectedSlot == null) {
            throw new DoubleBookingException("All matching parking slots are already booked for the requested timeframe.");
        }

        // 5. Reserve Slot & Update Lot Capacity
        selectedSlot.setStatus(SlotStatus.RESERVED);
        parkingSlotRepository.save(selectedSlot);

        if (lot.getAvailableCapacity() > 0) {
            lot.setAvailableCapacity(lot.getAvailableCapacity() - 1);
            parkingLotRepository.save(lot);
        }

        // 6. Calculate Estimated Fee
        BigDecimal estimatedFee = dynamicPricingService.calculateEstimatedFee(
                lot.getId(), request.getVehicleType(), request.getStartTime(), request.getEndTime());

        // 7. Create Reservation
        String ticketCode = TicketCodeGenerator.generateTicketCode();

        Reservation reservation = Reservation.builder()
                .ticketCode(ticketCode)
                .user(user)
                .slot(selectedSlot)
                .vehicle(vehicle)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(ReservationStatus.CONFIRMED)
                .estimatedAmount(estimatedFee)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        return mapToReservationResponse(savedReservation);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long userId, Long reservationId, boolean isAdmin) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        if (!isAdmin && !reservation.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view this reservation.");
        }

        return mapToReservationResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getUserReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToReservationResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReservationResponse cancelReservation(Long userId, Long reservationId, boolean isAdmin) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        if (!isAdmin && !reservation.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to cancel this reservation.");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED ||
            reservation.getStatus() == ReservationStatus.COMPLETED ||
            reservation.getStatus() == ReservationStatus.EXPIRED) {
            throw new InvalidOperationException("Reservation cannot be cancelled in its current status: " + reservation.getStatus());
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);

        // Release slot back to AVAILABLE
        ParkingSlot slot = reservation.getSlot();
        if (slot.getStatus() == SlotStatus.RESERVED) {
            slot.setStatus(SlotStatus.AVAILABLE);
            parkingSlotRepository.save(slot);
        }

        // Increment lot capacity
        ParkingLot lot = slot.getLot();
        if (lot.getAvailableCapacity() < lot.getTotalCapacity()) {
            lot.setAvailableCapacity(lot.getAvailableCapacity() + 1);
            parkingLotRepository.save(lot);
        }

        return mapToReservationResponse(saved);
    }

    private ReservationResponse mapToReservationResponse(Reservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .ticketCode(r.getTicketCode())
                .lotId(r.getSlot().getLot().getId())
                .lotName(r.getSlot().getLot().getName())
                .lotAddress(r.getSlot().getLot().getAddress())
                .slotId(r.getSlot().getId())
                .slotNumber(r.getSlot().getSlotNumber())
                .floorNumber(r.getSlot().getFloorNumber())
                .licensePlate(r.getVehicle().getLicensePlate())
                .vehicleType(r.getVehicle().getVehicleType())
                .startTime(r.getStartTime())
                .endTime(r.getEndTime())
                .status(r.getStatus())
                .estimatedAmount(r.getEstimatedAmount())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
