package com.parknow.dto.response;

import com.parknow.entity.enums.ReservationStatus;
import com.parknow.entity.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private Long id;
    private String ticketCode;
    private Long lotId;
    private String lotName;
    private String lotAddress;
    private Long slotId;
    private String slotNumber;
    private Integer floorNumber;
    private String licensePlate;
    private VehicleType vehicleType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;
    private BigDecimal estimatedAmount;
    private LocalDateTime createdAt;
}
