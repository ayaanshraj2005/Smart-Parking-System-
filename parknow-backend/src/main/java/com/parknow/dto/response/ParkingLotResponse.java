package com.parknow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingLotResponse {
    private Long id;
    private String name;
    private String address;
    private String city;
    private Integer totalCapacity;
    private Integer availableCapacity;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
