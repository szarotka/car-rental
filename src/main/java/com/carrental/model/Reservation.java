package com.carrental.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Reservation(UUID id, CarType carType, LocalDateTime start, int days, UUID carId, UUID customerId) {
}