package com.carrental.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(UUID id, String carType, LocalDateTime start, int days) {
}

