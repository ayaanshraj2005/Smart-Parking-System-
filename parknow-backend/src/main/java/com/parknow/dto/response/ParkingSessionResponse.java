package com.parknow.dto.response;

import com.parknow.entity.enums.SessionStatus;
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
public class ParkingSessionResponse {
    private Long id;
    private Long reservationId;
    private String ticketCode;
    private Long lotId;
    private String lotName;
    private Long slotId;
    private String slotNumber;
    private Integer floorNumber;
    private String licensePlate;
    private VehicleType vehicleType;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private SessionStatus status;
    private Long durationMinutes;
    private BigDecimal baseFee;
    private BigDecimal overstayFee;
    private BigDecimal totalFee;
    private PaymentResponse paymentDetails;
}
