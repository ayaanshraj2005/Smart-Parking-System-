package com.parknow.repository;

import com.parknow.entity.ParkingSlot;
import com.parknow.entity.enums.SlotStatus;
import com.parknow.entity.enums.VehicleType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {

    List<ParkingSlot> findByLotId(Long lotId);

    List<ParkingSlot> findByLotIdAndVehicleTypeAndStatus(Long lotId, VehicleType vehicleType, SlotStatus status);

    List<ParkingSlot> findByLotIdAndVehicleTypeAndStatusNotIn(Long lotId, VehicleType vehicleType, List<SlotStatus> excludedStatuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ParkingSlot s WHERE s.id = :id")
    Optional<ParkingSlot> findByIdWithLock(@Param("id") Long id);

    Boolean existsByLotIdAndSlotNumber(Long lotId, String slotNumber);

    long countByStatus(SlotStatus status);

    @Query("SELECT s.vehicleType, COUNT(s) FROM ParkingSlot s GROUP BY s.vehicleType")
    List<Object[]> getVehicleTypeDistribution();

    @Query("SELECT s.lot.id, s.lot.name, COUNT(s), " +
           "SUM(CASE WHEN s.status = 'OCCUPIED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN s.status = 'AVAILABLE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN s.status = 'MAINTENANCE' THEN 1 ELSE 0 END) " +
           "FROM ParkingSlot s GROUP BY s.lot.id, s.lot.name")
    List<Object[]> getLotOccupancyStats();
}
