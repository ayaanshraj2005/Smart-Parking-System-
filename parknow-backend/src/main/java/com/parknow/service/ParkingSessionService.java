package com.parknow.service;

import com.parknow.dto.request.SessionEntryRequest;
import com.parknow.dto.request.SessionExitRequest;
import com.parknow.dto.response.ParkingSessionResponse;

import java.util.List;

public interface ParkingSessionService {
    ParkingSessionResponse startSession(Long userId, SessionEntryRequest request);
    ParkingSessionResponse endSession(Long userId, Long sessionId, SessionExitRequest request, boolean isAdmin);
    List<ParkingSessionResponse> getActiveSessions(Long userId, boolean isAdmin);
    List<ParkingSessionResponse> getSessionHistory(Long userId, boolean isAdmin);
    ParkingSessionResponse getSessionById(Long userId, Long sessionId, boolean isAdmin);
}
