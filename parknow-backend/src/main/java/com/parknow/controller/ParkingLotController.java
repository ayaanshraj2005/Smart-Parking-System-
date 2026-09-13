package com.parknow.controller;

import com.parknow.dto.response.ApiResponse;
import com.parknow.dto.response.ParkingLotResponse;
import com.parknow.service.ParkingLotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-lots")
@RequiredArgsConstructor
public class ParkingLotController {

    private final ParkingLotService lotService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ParkingLotResponse>>> getAllLots(@RequestParam(required = false) String city) {
        List<ParkingLotResponse> lots = lotService.getAllLots(city);
        return ResponseEntity.ok(ApiResponse.success("Parking lots retrieved successfully", lots));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ParkingLotResponse>> getLotById(@PathVariable Long id) {
        ParkingLotResponse lot = lotService.getLotById(id);
        return ResponseEntity.ok(ApiResponse.success("Parking lot details fetched successfully", lot));
    }
}
