package com.parknow.controller;

import com.parknow.dto.request.SessionEntryRequest;
import com.parknow.dto.request.SessionExitRequest;
import com.parknow.dto.response.ApiResponse;
import com.parknow.dto.response.ParkingSessionResponse;
import com.parknow.security.UserPrincipal;
import com.parknow.service.ParkingSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class ParkingSessionController {

    private final ParkingSessionService parkingSessionService;

    @PostMapping("/entry")
    public ResponseEntity<ApiResponse<ParkingSessionResponse>> startSession(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody SessionEntryRequest request) {
        ParkingSessionResponse response = parkingSessionService.startSession(userPrincipal.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Parking session started successfully", response), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/exit")
    public ResponseEntity<ApiResponse<ParkingSessionResponse>> endSession(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @RequestBody(required = false) SessionExitRequest request) {
        boolean isAdmin = isUserAdmin(userPrincipal);
        ParkingSessionResponse response = parkingSessionService.endSession(userPrincipal.getId(), id, request, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Parking session completed and billing processed", response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ParkingSessionResponse>>> getActiveSessions(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        boolean isAdmin = isUserAdmin(userPrincipal);
        List<ParkingSessionResponse> sessions = parkingSessionService.getActiveSessions(userPrincipal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Active parking sessions fetched successfully", sessions));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ParkingSessionResponse>>> getSessionHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        boolean isAdmin = isUserAdmin(userPrincipal);
        List<ParkingSessionResponse> sessions = parkingSessionService.getSessionHistory(userPrincipal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Parking session history fetched successfully", sessions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ParkingSessionResponse>> getSessionById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        boolean isAdmin = isUserAdmin(userPrincipal);
        ParkingSessionResponse response = parkingSessionService.getSessionById(userPrincipal.getId(), id, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Parking session details fetched successfully", response));
    }

    private boolean isUserAdmin(UserPrincipal userPrincipal) {
        return userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
