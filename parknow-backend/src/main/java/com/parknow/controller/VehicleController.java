package com.parknow.controller;

import com.parknow.dto.request.VehicleRequest;
import com.parknow.dto.response.ApiResponse;
import com.parknow.dto.response.VehicleResponse;
import com.parknow.security.UserPrincipal;
import com.parknow.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> addVehicle(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.addVehicle(userPrincipal.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Vehicle registered successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getUserVehicles(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<VehicleResponse> vehicles = vehicleService.getUserVehicles(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Vehicles retrieved successfully", vehicles));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        vehicleService.deleteVehicle(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle deleted successfully", null));
    }
}
