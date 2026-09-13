package com.parknow.service;

import com.parknow.dto.request.ParkingSlotRequest;
import com.parknow.dto.request.UpdateSlotRequest;
import com.parknow.dto.response.ParkingSlotResponse;
import com.parknow.entity.enums.SlotStatus;
import com.parknow.entity.enums.VehicleType;

import java.util.List;

public interface ParkingSlotService {
    ParkingSlotResponse createSlot(ParkingSlotRequest request);
    ParkingSlotResponse updateSlot(Long slotId, UpdateSlotRequest request);
    ParkingSlotResponse updateSlotStatus(Long slotId, SlotStatus status);
    List<ParkingSlotResponse> getSlotsByLot(Long lotId);
    List<ParkingSlotResponse> getAvailableSlots(Long lotId, VehicleType vehicleType);
}
