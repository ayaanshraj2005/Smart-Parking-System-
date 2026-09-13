package com.parknow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OccupancyReportResponse {
    private long totalSlots;
    private long availableSlots;
    private long occupiedSlots;
    private long maintenanceSlots;
    private double overallOccupancyPercentage;
    private List<LotOccupancyStat> lotOccupancyList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LotOccupancyStat {
        private Long lotId;
        private String lotName;
        private long totalCapacity;
        private long availableCapacity;
        private long occupiedSlots;
        private double occupancyPercentage;
    }
}
