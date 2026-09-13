package com.parknow.repository;

import com.parknow.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
    List<ParkingLot> findByCityIgnoreCaseAndIsActiveTrue(String city);
    List<ParkingLot> findByIsActiveTrue();
}
