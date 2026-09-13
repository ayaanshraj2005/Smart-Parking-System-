package com.parknow.controller;

import com.parknow.dto.request.CreateReservationRequest;
import com.parknow.dto.response.ApiResponse;
import com.parknow.dto.response.ReservationResponse;
import com.parknow.security.UserPrincipal;
import com.parknow.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(userPrincipal.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Reservation confirmed successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ReservationResponse> reservations = reservationService.getUserReservations(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("User reservations fetched successfully", reservations));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservationById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        boolean isAdmin = isUserAdmin(userPrincipal);
        ReservationResponse response = reservationService.getReservationById(userPrincipal.getId(), id, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Reservation details fetched successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        boolean isAdmin = isUserAdmin(userPrincipal);
        ReservationResponse response = reservationService.cancelReservation(userPrincipal.getId(), id, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Reservation cancelled successfully", response));
    }

    private boolean isUserAdmin(UserPrincipal userPrincipal) {
        return userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
