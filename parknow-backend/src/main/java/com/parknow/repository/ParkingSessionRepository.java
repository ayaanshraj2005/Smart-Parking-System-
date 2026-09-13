package com.parknow.repository;

import com.parknow.entity.ParkingSession;
import com.parknow.entity.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {
    Optional<ParkingSession> findByReservationId(Long reservationId);
    Optional<ParkingSession> findByReservationTicketCode(String ticketCode);
    Optional<ParkingSession> findByReservationTicketCodeAndStatus(String ticketCode, SessionStatus status);
    Optional<ParkingSession> findByVehicleLicensePlateAndStatus(String licensePlate, SessionStatus status);
    Optional<ParkingSession> findBySlotIdAndStatus(Long slotId, SessionStatus status);

    List<ParkingSession> findByStatus(SessionStatus status);
    List<ParkingSession> findBySlotLotId(Long lotId);

    List<ParkingSession> findByReservationUserIdAndStatus(Long userId, SessionStatus status);
    List<ParkingSession> findByReservationUserIdOrderByEntryTimeDesc(Long userId);
    List<ParkingSession> findAllByOrderByEntryTimeDesc();
}
