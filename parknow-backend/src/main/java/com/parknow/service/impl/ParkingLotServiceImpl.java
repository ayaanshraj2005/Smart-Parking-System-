package com.parknow.service.impl;

import com.parknow.dto.request.ParkingLotRequest;
import com.parknow.dto.response.ParkingLotResponse;
import com.parknow.entity.ParkingLot;
import com.parknow.exception.ResourceNotFoundException;
import com.parknow.repository.ParkingLotRepository;
import com.parknow.service.ParkingLotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingLotServiceImpl implements ParkingLotService {

    private final ParkingLotRepository lotRepository;

    @Override
    @Transactional
    public ParkingLotResponse createLot(ParkingLotRequest request) {
        ParkingLot lot = ParkingLot.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .totalCapacity(request.getTotalCapacity())
                .availableCapacity(request.getTotalCapacity())
                .isActive(true)
                .build();

        ParkingLot savedLot = lotRepository.save(lot);
        return mapToLotResponse(savedLot);
    }

    @Override
    @Transactional
    public ParkingLotResponse updateLot(Long id, ParkingLotRequest request) {
        ParkingLot lot = lotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + id));

        lot.setName(request.getName());
        lot.setAddress(request.getAddress());
        lot.setCity(request.getCity());

        // Update total capacity and adjust available capacity proportionately
        int capacityDiff = request.getTotalCapacity() - lot.getTotalCapacity();
        lot.setTotalCapacity(request.getTotalCapacity());
        lot.setAvailableCapacity(Math.max(0, lot.getAvailableCapacity() + capacityDiff));

        ParkingLot updatedLot = lotRepository.save(lot);
        return mapToLotResponse(updatedLot);
    }

    @Override
    @Transactional
    public void deactivateLot(Long id) {
        ParkingLot lot = lotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + id));
        lot.setIsActive(false);
        lotRepository.save(lot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingLotResponse> getAllLots(String city) {
        List<ParkingLot> lots;
        if (StringUtils.hasText(city)) {
            lots = lotRepository.findByCityIgnoreCaseAndIsActiveTrue(city);
        } else {
            lots = lotRepository.findByIsActiveTrue();
        }
        return lots.stream().map(this::mapToLotResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ParkingLotResponse getLotById(Long id) {
        ParkingLot lot = lotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with id: " + id));
        return mapToLotResponse(lot);
    }

    private ParkingLotResponse mapToLotResponse(ParkingLot lot) {
        return ParkingLotResponse.builder()
                .id(lot.getId())
                .name(lot.getName())
                .address(lot.getAddress())
                .city(lot.getCity())
                .totalCapacity(lot.getTotalCapacity())
                .availableCapacity(lot.getAvailableCapacity())
                .isActive(lot.getIsActive())
                .createdAt(lot.getCreatedAt())
                .build();
    }
}
