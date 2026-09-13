package com.parknow.service.impl;

import com.parknow.dto.request.VehicleRequest;
import com.parknow.dto.response.VehicleResponse;
import com.parknow.entity.User;
import com.parknow.entity.Vehicle;
import com.parknow.exception.InvalidOperationException;
import com.parknow.exception.ResourceNotFoundException;
import com.parknow.repository.UserRepository;
import com.parknow.repository.VehicleRepository;
import com.parknow.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public VehicleResponse addVehicle(Long userId, VehicleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (vehicleRepository.existsByLicensePlate(request.getLicensePlate().toUpperCase())) {
            throw new InvalidOperationException("Vehicle with license plate " + request.getLicensePlate() + " is already registered.");
        }

        Vehicle vehicle = Vehicle.builder()
                .user(user)
                .licensePlate(request.getLicensePlate().toUpperCase())
                .vehicleType(request.getVehicleType())
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return mapToVehicleResponse(savedVehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getUserVehicles(Long userId) {
        return vehicleRepository.findByUserId(userId).stream()
                .map(this::mapToVehicleResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteVehicle(Long userId, Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        if (!vehicle.getUser().getId().equals(userId)) {
            throw new InvalidOperationException("You are not authorized to delete this vehicle.");
        }

        vehicleRepository.delete(vehicle);
    }

    private VehicleResponse mapToVehicleResponse(Vehicle v) {
        return VehicleResponse.builder()
                .id(v.getId())
                .licensePlate(v.getLicensePlate())
                .vehicleType(v.getVehicleType())
                .createdAt(v.getCreatedAt())
                .build();
    }
}
