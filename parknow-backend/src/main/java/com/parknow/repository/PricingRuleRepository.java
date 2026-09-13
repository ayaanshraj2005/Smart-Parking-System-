package com.parknow.repository;

import com.parknow.entity.PricingRule;
import com.parknow.entity.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    List<PricingRule> findByLotId(Long lotId);
    Optional<PricingRule> findByLotIdAndVehicleType(Long lotId, VehicleType vehicleType);
}
