package com.parknow.service.impl;

import com.parknow.dto.request.ParkingSlotRequest;
import com.parknow.dto.request.UpdateSlotRequest;
import com.parknow.dto.response.ParkingSlotResponse;
import com.parknow.entity.ParkingLot;
import com.parknow.entity.ParkingSlot;
import com.parknow.entity.enums.SlotStatus;
import com.parknow.entity.enums.VehicleType;
import com.parknow.exception.InvalidOperationException;
import com.parknow.exception.ResourceNotFoundException;
import com.parknow.repository.ParkingLotRepository;
import com.parknow.repository.ParkingSlotRepository;
import com.parknow.service.ParkingSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingSlotServiceImpl implements ParkingSlotService {

    private final ParkingSlotRepository slotRepository;
    private final ParkingLotRepository lotRepository;

    @Override
    @Transactional
    public ParkingSlotResponse createSlot(ParkingSlotRequest request) {
        ParkingLot lot = lotRepository.findById(request.getLotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + request.getLotId()));

        if (slotRepository.existsByLotIdAndSlotNumber(lot.getId(), request.getSlotNumber())) {
            throw new InvalidOperationException("Slot number " + request.getSlotNumber() + " already exists in this parking lot.");
        }

        ParkingSlot slot = ParkingSlot.builder()
                .lot(lot)
                .slotNumber(request.getSlotNumber())
                .floorNumber(request.getFloorNumber())
                .vehicleType(request.getVehicleType())
                .status(SlotStatus.AVAILABLE)
                .build();

        ParkingSlot savedSlot = slotRepository.save(slot);
        return mapToSlotResponse(savedSlot);
    }

    @Override
    @Transactional
    public ParkingSlotResponse updateSlot(Long slotId, UpdateSlotRequest request) {
        ParkingSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with id: " + slotId));

        if (request.getVehicleType() != null) {
            slot.setVehicleType(request.getVehicleType());
        }

        if (request.getStatus() != null && request.getStatus() != slot.getStatus()) {
            updateSlotStatusInternal(slot, request.getStatus());
        }

        ParkingSlot updatedSlot = slotRepository.save(slot);
        return mapToSlotResponse(updatedSlot);
    }

    @Override
    @Transactional
    public ParkingSlotResponse updateSlotStatus(Long slotId, SlotStatus status) {
        ParkingSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with id: " + slotId));

        if (slot.getStatus() != status) {
            updateSlotStatusInternal(slot, status);
        }

        ParkingSlot updatedSlot = slotRepository.save(slot);
        return mapToSlotResponse(updatedSlot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingSlotResponse> getSlotsByLot(Long lotId) {
        return slotRepository.findByLotId(lotId).stream()
                .map(this::mapToSlotResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingSlotResponse> getAvailableSlots(Long lotId, VehicleType vehicleType) {
        // Business Rule: ONLY return slots with status == SlotStatus.AVAILABLE.
        // Slots marked MAINTENANCE, DISABLED, OCCUPIED, or RESERVED are strictly filtered out.
        return slotRepository.findByLotIdAndVehicleTypeAndStatus(lotId, vehicleType, SlotStatus.AVAILABLE).stream()
                .map(this::mapToSlotResponse)
                .toList();
    }

    private void updateSlotStatusInternal(ParkingSlot slot, SlotStatus newStatus) {
        ParkingLot lot = slot.getLot();
        SlotStatus oldStatus = slot.getStatus();

        // Adjust available capacity in lot if availability state transitions
        if (oldStatus == SlotStatus.AVAILABLE && newStatus != SlotStatus.AVAILABLE) {
            lot.setAvailableCapacity(Math.max(0, lot.getAvailableCapacity() - 1));
            lotRepository.save(lot);
        } else if (oldStatus != SlotStatus.AVAILABLE && newStatus == SlotStatus.AVAILABLE) {
            lot.setAvailableCapacity(Math.min(lot.getTotalCapacity(), lot.getAvailableCapacity() + 1));
            lotRepository.save(lot);
        }

        slot.setStatus(newStatus);
    }

    private ParkingSlotResponse mapToSlotResponse(ParkingSlot slot) {
        return ParkingSlotResponse.builder()
                .id(slot.getId())
                .lotId(slot.getLot().getId())
                .lotName(slot.getLot().getName())
                .slotNumber(slot.getSlotNumber())
                .floorNumber(slot.getFloorNumber())
                .vehicleType(slot.getVehicleType())
                .status(slot.getStatus())
                .build();
    }
}
