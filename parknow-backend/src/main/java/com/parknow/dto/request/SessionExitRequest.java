package com.parknow.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionExitRequest {

    private LocalDateTime exitTime; // Optional exit timestamp override for testing / simulation

    @Builder.Default
    private String paymentMethod = "DIGITAL_WALLET";
}
