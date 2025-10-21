package com.carrental.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReservationResponse {
    private final UUID id;
    private final String carType;
    private final LocalDateTime start;
    private final int days;
}

