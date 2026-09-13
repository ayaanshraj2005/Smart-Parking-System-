package com.parknow.controller;

import com.parknow.dto.response.ApiResponse;
import com.parknow.dto.response.ParkingSlotResponse;
import com.parknow.entity.enums.VehicleType;
import com.parknow.service.ParkingSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-slots")
@RequiredArgsConstructor
public class ParkingSlotController {

    private final ParkingSlotService slotService;

    @GetMapping("/lot/{lotId}")
    public ResponseEntity<ApiResponse<List<ParkingSlotResponse>>> getSlotsByLot(@PathVariable Long lotId) {
        List<ParkingSlotResponse> slots = slotService.getSlotsByLot(lotId);
        return ResponseEntity.ok(ApiResponse.success("Parking slots fetched successfully", slots));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<ParkingSlotResponse>>> getAvailableSlots(
            @RequestParam Long lotId,
            @RequestParam VehicleType vehicleType) {
        List<ParkingSlotResponse> slots = slotService.getAvailableSlots(lotId, vehicleType);
        return ResponseEntity.ok(ApiResponse.success("Available slots fetched successfully", slots));
    }
}
