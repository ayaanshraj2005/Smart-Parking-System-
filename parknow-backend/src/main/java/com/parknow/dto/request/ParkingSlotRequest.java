package com.parknow.dto.request;

import com.parknow.entity.enums.VehicleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSlotRequest {

    @NotNull(message = "Parking lot ID is required")
    private Long lotId;

    @NotBlank(message = "Slot number is required")
    @Size(max = 20, message = "Slot number cannot exceed 20 characters")
    private String slotNumber;

    @NotNull(message = "Floor number is required")
    @Min(value = 0, message = "Floor number cannot be negative")
    private Integer floorNumber;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;
}
