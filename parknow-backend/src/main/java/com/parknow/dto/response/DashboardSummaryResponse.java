package com.parknow.dto.response;

import com.parknow.entity.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {
    private long totalUsers;
    private long totalParkingLots;
    private long totalParkingSlots;
    private long availableSlots;
    private long occupiedSlots;
    private long activeReservations;
    private long completedReservations;
    private BigDecimal todaysRevenue;
    private BigDecimal monthlyRevenue;
    private double occupancyPercentage;
    private Map<VehicleType, Long> vehicleTypeDistribution;
    private List<MostUsedLotStat> mostUsedParkingLots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MostUsedLotStat {
        private Long lotId;
        private String lotName;
        private long reservationCount;
    }
}
