package com.parknow.dto.request;

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
public class UpdateSlotRequest {
    private VehicleType vehicleType;
    private SlotStatus status;
}
