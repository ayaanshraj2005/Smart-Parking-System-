package com.parknow.service;

import com.parknow.dto.request.VehicleRequest;
import com.parknow.dto.response.VehicleResponse;

import java.util.List;

public interface VehicleService {
    VehicleResponse addVehicle(Long userId, VehicleRequest request);
    List<VehicleResponse> getUserVehicles(Long userId);
    void deleteVehicle(Long userId, Long vehicleId);
}
