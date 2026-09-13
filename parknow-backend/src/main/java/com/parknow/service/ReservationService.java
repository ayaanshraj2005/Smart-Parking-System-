package com.parknow.service;

import com.parknow.dto.request.CreateReservationRequest;
import com.parknow.dto.response.ReservationResponse;

import java.util.List;

public interface ReservationService {
    ReservationResponse createReservation(Long userId, CreateReservationRequest request);
    ReservationResponse getReservationById(Long userId, Long reservationId, boolean isAdmin);
    List<ReservationResponse> getUserReservations(Long userId);
    ReservationResponse cancelReservation(Long userId, Long reservationId, boolean isAdmin);
}
