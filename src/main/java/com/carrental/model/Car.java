package com.carrental.model;

import java.util.UUID;

public record Car(UUID id, CarType type, String registrationNumber) {

}

