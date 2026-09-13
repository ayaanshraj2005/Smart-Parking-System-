package com.parknow.service.impl;

import com.parknow.dto.response.*;
import com.parknow.entity.ParkingSlot;
import com.parknow.entity.Reservation;
import com.parknow.entity.Role;
import com.parknow.entity.User;
import com.parknow.entity.enums.ReservationStatus;
import com.parknow.entity.enums.SlotStatus;
import com.parknow.entity.enums.VehicleType;
import com.parknow.repository.*;
import com.parknow.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        long totalUsers = userRepository.count();
        long totalParkingLots = parkingLotRepository.count();
        long totalParkingSlots = parkingSlotRepository.count();

        long availableSlots = parkingSlotRepository.countByStatus(SlotStatus.AVAILABLE);
        long occupiedSlots = parkingSlotRepository.countByStatus(SlotStatus.OCCUPIED);

        long activeReservations = reservationRepository.countByStatusIn(
                List.of(ReservationStatus.CONFIRMED, ReservationStatus.ACTIVE));
        long completedReservations = reservationRepository.countByStatus(ReservationStatus.COMPLETED);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.with(LocalTime.MIN);
        LocalDateTime endOfToday = now.with(LocalTime.MAX);

        LocalDateTime startOfMonth = now.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).with(LocalTime.MAX);

        BigDecimal todaysRevenue = paymentRepository.getRevenueBetween(startOfToday, endOfToday);
        BigDecimal monthlyRevenue = paymentRepository.getRevenueBetween(startOfMonth, endOfMonth);

        double occupancyPercentage = totalParkingSlots > 0
                ? Math.round((occupiedSlots * 100.0 / totalParkingSlots) * 100.0) / 100.0
                : 0.0;

        // Vehicle distribution
        Map<VehicleType, Long> vehicleDistribution = new EnumMap<>(VehicleType.class);
        for (VehicleType vt : VehicleType.values()) {
            vehicleDistribution.put(vt, 0L);
        }
        List<Object[]> distRows = parkingSlotRepository.getVehicleTypeDistribution();
        for (Object[] row : distRows) {
            VehicleType vt = (VehicleType) row[0];
            Long count = (Long) row[1];
            vehicleDistribution.put(vt, count);
        }

        // Top used parking lots
        List<Object[]> topRows = reservationRepository.getMostUsedParkingLots();
        List<DashboardSummaryResponse.MostUsedLotStat> topLots = topRows.stream()
                .limit(5)
                .map(row -> DashboardSummaryResponse.MostUsedLotStat.builder()
                        .lotId((Long) row[0])
                        .lotName((String) row[1])
                        .reservationCount((Long) row[2])
                        .build())
                .toList();

        return DashboardSummaryResponse.builder()
                .totalUsers(totalUsers)
                .totalParkingLots(totalParkingLots)
                .totalParkingSlots(totalParkingSlots)
                .availableSlots(availableSlots)
                .occupiedSlots(occupiedSlots)
                .activeReservations(activeReservations)
                .completedReservations(completedReservations)
                .todaysRevenue(todaysRevenue.setScale(2, RoundingMode.HALF_UP))
                .monthlyRevenue(monthlyRevenue.setScale(2, RoundingMode.HALF_UP))
                .occupancyPercentage(occupancyPercentage)
                .vehicleTypeDistribution(vehicleDistribution)
                .mostUsedParkingLots(topLots)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueReportResponse getRevenueReport() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.with(LocalTime.MIN);
        LocalDateTime endOfToday = now.with(LocalTime.MAX);

        LocalDateTime startOfMonth = now.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).with(LocalTime.MAX);

        BigDecimal todaysRevenue = paymentRepository.getRevenueBetween(startOfToday, endOfToday);
        BigDecimal monthlyRevenue = paymentRepository.getRevenueBetween(startOfMonth, endOfMonth);
        BigDecimal totalRevenue = paymentRepository.getTotalRevenue();

        List<Object[]> lotRows = paymentRepository.getRevenueByLot();
        List<RevenueReportResponse.LotRevenueStat> lotStats = lotRows.stream()
                .map(row -> RevenueReportResponse.LotRevenueStat.builder()
                        .lotId((Long) row[0])
                        .lotName((String) row[1])
                        .totalRevenue(((BigDecimal) row[2]).setScale(2, RoundingMode.HALF_UP))
                        .build())
                .toList();

        List<Object[]> typeRows = paymentRepository.getRevenueByVehicleType();
        List<RevenueReportResponse.VehicleTypeRevenueStat> typeStats = typeRows.stream()
                .map(row -> RevenueReportResponse.VehicleTypeRevenueStat.builder()
                        .vehicleType((VehicleType) row[0])
                        .totalRevenue(((BigDecimal) row[1]).setScale(2, RoundingMode.HALF_UP))
                        .build())
                .toList();

        return RevenueReportResponse.builder()
                .todaysRevenue(todaysRevenue.setScale(2, RoundingMode.HALF_UP))
                .monthlyRevenue(monthlyRevenue.setScale(2, RoundingMode.HALF_UP))
                .totalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP))
                .revenueByLot(lotStats)
                .revenueByVehicleType(typeStats)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OccupancyReportResponse getOccupancyReport() {
        long totalSlots = parkingSlotRepository.count();
        long availableSlots = parkingSlotRepository.countByStatus(SlotStatus.AVAILABLE);
        long occupiedSlots = parkingSlotRepository.countByStatus(SlotStatus.OCCUPIED);
        long maintenanceSlots = parkingSlotRepository.countByStatus(SlotStatus.MAINTENANCE);

        double overallPct = totalSlots > 0
                ? Math.round((occupiedSlots * 100.0 / totalSlots) * 100.0) / 100.0
                : 0.0;

        List<Object[]> lotRows = parkingSlotRepository.getLotOccupancyStats();
        List<OccupancyReportResponse.LotOccupancyStat> lotStats = lotRows.stream()
                .map(row -> {
                    Long lotId = (Long) row[0];
                    String lotName = (String) row[1];
                    long totalCap = ((Number) row[2]).longValue();
                    long occupied = ((Number) row[3]).longValue();
                    long available = ((Number) row[4]).longValue();
                    double pct = totalCap > 0 ? Math.round((occupied * 100.0 / totalCap) * 100.0) / 100.0 : 0.0;

                    return OccupancyReportResponse.LotOccupancyStat.builder()
                            .lotId(lotId)
                            .lotName(lotName)
                            .totalCapacity(totalCap)
                            .occupiedSlots(occupied)
                            .availableCapacity(available)
                            .occupancyPercentage(pct)
                            .build();
                })
                .toList();

        return OccupancyReportResponse.builder()
                .totalSlots(totalSlots)
                .availableSlots(availableSlots)
                .occupiedSlots(occupiedSlots)
                .maintenanceSlots(maintenanceSlots)
                .overallOccupancyPercentage(overallPct)
                .lotOccupancyList(lotStats)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReservationResponse> getAllReservations(ReservationStatus statusFilter, Pageable pageable) {
        Page<Reservation> page = (statusFilter != null)
                ? reservationRepository.findByStatus(statusFilter, pageable)
                : reservationRepository.findAll(pageable);

        Page<ReservationResponse> responsePage = page.map(this::mapToReservationResponse);
        return PagedResponse.fromPage(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AdminUserResponse> getAllUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);

        Page<AdminUserResponse> responsePage = page.map(user -> {
            Set<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .map(Enum::name)
                    .collect(Collectors.toSet());

            int vehicleCount = (int) vehicleRepository.countByUserId(user.getId());

            return AdminUserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .phoneNumber(user.getPhoneNumber())
                    .roles(roles)
                    .vehicleCount(vehicleCount)
                    .createdAt(user.getCreatedAt())
                    .build();
        });

        return PagedResponse.fromPage(responsePage);
    }

    private ReservationResponse mapToReservationResponse(Reservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .ticketCode(r.getTicketCode())
                .lotId(r.getSlot().getLot().getId())
                .lotName(r.getSlot().getLot().getName())
                .lotAddress(r.getSlot().getLot().getAddress())
                .slotId(r.getSlot().getId())
                .slotNumber(r.getSlot().getSlotNumber())
                .floorNumber(r.getSlot().getFloorNumber())
                .licensePlate(r.getVehicle().getLicensePlate())
                .vehicleType(r.getVehicle().getVehicleType())
                .startTime(r.getStartTime())
                .endTime(r.getEndTime())
                .status(r.getStatus())
                .estimatedAmount(r.getEstimatedAmount())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
