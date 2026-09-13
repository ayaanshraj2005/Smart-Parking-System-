package com.parknow.dto.response;

import com.parknow.entity.enums.SlotStatus;
import com.parknow.entity.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSlotResponse {
    private Long id;
    private Long lotId;
    private String lotName;
    private String slotNumber;
    private Integer floorNumber;
    private VehicleType vehicleType;
    private SlotStatus status;
}
