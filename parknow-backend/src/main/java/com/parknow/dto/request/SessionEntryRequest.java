package com.parknow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionEntryRequest {

    @NotBlank(message = "Ticket code is required")
    private String ticketCode;

    private LocalDateTime entryTime; // Optional timestamp override for testing / manual entry logging
}
