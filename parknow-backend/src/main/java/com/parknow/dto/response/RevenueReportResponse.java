package com.parknow.dto.response;

import com.parknow.entity.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReportResponse {
    private BigDecimal todaysRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal totalRevenue;
    private List<LotRevenueStat> revenueByLot;
    private List<VehicleTypeRevenueStat> revenueByVehicleType;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LotRevenueStat {
        private Long lotId;
        private String lotName;
        private BigDecimal totalRevenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VehicleTypeRevenueStat {
        private VehicleType vehicleType;
        private BigDecimal totalRevenue;
    }
}
