package com.parknow.controller;

import com.parknow.dto.request.ParkingLotRequest;
import com.parknow.dto.request.ParkingSlotRequest;
import com.parknow.dto.request.UpdateSlotRequest;
import com.parknow.dto.response.ApiResponse;
import com.parknow.dto.response.ParkingLotResponse;
import com.parknow.dto.response.ParkingSlotResponse;
import com.parknow.entity.enums.SlotStatus;
import com.parknow.service.ParkingLotService;
import com.parknow.service.ParkingSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminParkingLotController {

    private final ParkingLotService lotService;
    private final ParkingSlotService slotService;

    @PostMapping("/parking-lots")
    public ResponseEntity<ApiResponse<ParkingLotResponse>> createLot(@Valid @RequestBody ParkingLotRequest request) {
        ParkingLotResponse response = lotService.createLot(request);
        return new ResponseEntity<>(ApiResponse.success("Parking lot created successfully", response), HttpStatus.CREATED);
    }

    @PutMapping("/parking-lots/{id}")
    public ResponseEntity<ApiResponse<ParkingLotResponse>> updateLot(@PathVariable Long id, @Valid @RequestBody ParkingLotRequest request) {
        ParkingLotResponse response = lotService.updateLot(id, request);
        return ResponseEntity.ok(ApiResponse.success("Parking lot updated successfully", response));
    }

    @DeleteMapping("/parking-lots/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateLot(@PathVariable Long id) {
        lotService.deactivateLot(id);
        return ResponseEntity.ok(ApiResponse.success("Parking lot deactivated successfully"));
    }

    @PostMapping("/slots")
    public ResponseEntity<ApiResponse<ParkingSlotResponse>> createSlot(@Valid @RequestBody ParkingSlotRequest request) {
        ParkingSlotResponse response = slotService.createSlot(request);
        return new ResponseEntity<>(ApiResponse.success("Parking slot created successfully", response), HttpStatus.CREATED);
    }

    @PutMapping("/slots/{id}")
    public ResponseEntity<ApiResponse<ParkingSlotResponse>> updateSlot(@PathVariable Long id, @RequestBody UpdateSlotRequest request) {
        ParkingSlotResponse response = slotService.updateSlot(id, request);
        return ResponseEntity.ok(ApiResponse.success("Parking slot updated successfully", response));
    }

    @PatchMapping("/slots/{id}/status")
    public ResponseEntity<ApiResponse<ParkingSlotResponse>> updateSlotStatus(@PathVariable Long id, @RequestParam SlotStatus status) {
        ParkingSlotResponse response = slotService.updateSlotStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Parking slot status updated to " + status, response));
    }
}
