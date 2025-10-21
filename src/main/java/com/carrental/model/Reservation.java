package com.carrental.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;


@Data
@AllArgsConstructor
public class Reservation {
    UUID id;
    CarType carType;
    java.time.LocalDateTime start;
    int days;

}