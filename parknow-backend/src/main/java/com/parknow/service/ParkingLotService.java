package com.parknow.service;

import com.parknow.dto.request.ParkingLotRequest;
import com.parknow.dto.response.ParkingLotResponse;

import java.util.List;

public interface ParkingLotService {
    ParkingLotResponse createLot(ParkingLotRequest request);
    ParkingLotResponse updateLot(Long id, ParkingLotRequest request);
    void deactivateLot(Long id);
    List<ParkingLotResponse> getAllLots(String city);
    ParkingLotResponse getLotById(Long id);
}
