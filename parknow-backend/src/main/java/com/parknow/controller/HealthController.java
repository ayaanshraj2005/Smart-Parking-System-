package com.parknow.controller;

import com.parknow.dto.response.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealthStatus() {
        HealthResponse response = HealthResponse.builder()
                .status("UP")
                .service("ParkNow API")
                .build();
        return ResponseEntity.ok(response);
    }
}
