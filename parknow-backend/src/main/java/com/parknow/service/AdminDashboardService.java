package com.parknow.service;

import com.parknow.dto.response.*;
import com.parknow.entity.enums.ReservationStatus;
import org.springframework.data.domain.Pageable;

public interface AdminDashboardService {
    DashboardSummaryResponse getDashboardSummary();
    RevenueReportResponse getRevenueReport();
    OccupancyReportResponse getOccupancyReport();
    PagedResponse<ReservationResponse> getAllReservations(ReservationStatus statusFilter, Pageable pageable);
    PagedResponse<AdminUserResponse> getAllUsers(Pageable pageable);
}
