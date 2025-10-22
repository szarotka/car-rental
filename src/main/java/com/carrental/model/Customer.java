package com.carrental.model;

import java.util.UUID;

public record Customer(UUID id, String firstName, String lastName, String phone) {
}

