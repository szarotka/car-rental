package com.carrental.repository;

import com.carrental.model.CarInventory;
import com.carrental.model.CarType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CarRepository {

    private final List<CarInventory> carInventories = List.of(
            new CarInventory(CarType.SEDAN, 1),
            new CarInventory(CarType.SUV, 2),
            new CarInventory(CarType.VAN, 1)
    );

    public List<CarInventory> getCars() {
        return carInventories;
    }

}
