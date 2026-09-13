package com.parknow.repository;

import com.parknow.entity.Reservation;
import com.parknow.entity.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByTicketCode(String ticketCode);

    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Reservation> findBySlotIdAndStatusIn(Long slotId, List<ReservationStatus> statuses);

    @Query("SELECT r FROM Reservation r WHERE r.slot.id = :slotId AND r.status IN ('CONFIRMED', 'ACTIVE') " +
           "AND ((r.startTime < :endTime AND r.endTime > :startTime))")
    List<Reservation> findConflictingReservations(
        @Param("slotId") Long slotId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    long countByStatus(ReservationStatus status);

    long countByStatusIn(List<ReservationStatus> statuses);

    Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable);

    @Query("SELECT r.slot.lot.id, r.slot.lot.name, COUNT(r) FROM Reservation r GROUP BY r.slot.lot.id, r.slot.lot.name ORDER BY COUNT(r) DESC")
    List<Object[]> getMostUsedParkingLots();
}
