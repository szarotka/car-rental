package com.carrental.dto;

import com.carrental.model.CarType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationRequest(CarType carType, LocalDateTime start, int days, UUID customerId) {
}

