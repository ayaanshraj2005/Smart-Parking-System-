package com.parknow.controller;

import com.parknow.dto.response.*;
import com.parknow.entity.enums.ReservationStatus;
import com.parknow.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary() {
        DashboardSummaryResponse summary = adminDashboardService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard summary fetched successfully", summary));
    }

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getRevenueReport() {
        RevenueReportResponse report = adminDashboardService.getRevenueReport();
        return ResponseEntity.ok(ApiResponse.success("Revenue report fetched successfully", report));
    }

    @GetMapping("/occupancy")
    public ResponseEntity<ApiResponse<OccupancyReportResponse>> getOccupancyReport() {
        OccupancyReportResponse report = adminDashboardService.getOccupancyReport();
        return ResponseEntity.ok(ApiResponse.success("Occupancy report fetched successfully", report));
    }

    @GetMapping("/reservations")
    public ResponseEntity<ApiResponse<PagedResponse<ReservationResponse>>> getAllReservations(
            @RequestParam(required = false) ReservationStatus status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        PagedResponse<ReservationResponse> pagedReservations = adminDashboardService.getAllReservations(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Reservations fetched successfully", pagedReservations));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PagedResponse<AdminUserResponse>>> getAllUsers(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        PagedResponse<AdminUserResponse> pagedUsers = adminDashboardService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success("User list fetched successfully", pagedUsers));
    }
}
