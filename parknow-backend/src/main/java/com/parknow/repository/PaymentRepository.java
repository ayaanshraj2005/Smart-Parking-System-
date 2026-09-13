package com.parknow.repository;

import com.parknow.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserIdOrderByPaymentTimeDesc(Long userId);
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findBySessionId(Long sessionId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = 'COMPLETED' AND p.paymentTime >= :startTime AND p.paymentTime <= :endTime")
    BigDecimal getRevenueBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = 'COMPLETED'")
    BigDecimal getTotalRevenue();

    @Query("SELECT p.session.slot.lot.id, p.session.slot.lot.name, SUM(p.amount) FROM Payment p WHERE p.paymentStatus = 'COMPLETED' GROUP BY p.session.slot.lot.id, p.session.slot.lot.name")
    List<Object[]> getRevenueByLot();

    @Query("SELECT p.session.vehicle.vehicleType, SUM(p.amount) FROM Payment p WHERE p.paymentStatus = 'COMPLETED' GROUP BY p.session.vehicle.vehicleType")
    List<Object[]> getRevenueByVehicleType();
}
