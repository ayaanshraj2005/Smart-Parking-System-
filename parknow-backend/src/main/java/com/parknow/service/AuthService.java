package com.parknow.service;

import com.parknow.dto.request.LoginRequest;
import com.parknow.dto.request.RegisterRequest;
import com.parknow.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
