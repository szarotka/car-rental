package com.carrental.repository;

import com.carrental.model.Car;
import com.carrental.model.CarType;

import java.util.List;
import java.util.UUID;

public class CarRepository {

    private final List<Car> cars = List.of(
            new Car(UUID.randomUUID(), CarType.SEDAN, "ABC-123"),
            new Car(UUID.randomUUID(), CarType.SUV, "DEF-456"),
            new Car(UUID.randomUUID(), CarType.SUV, "JKL-123"),
            new Car(UUID.randomUUID(), CarType.VAN, "GHI-789")
    );

    public List<Car> getCars() {
        return cars;
    }

}
