package com.carrental.service;

import com.carrental.dto.ReservationRequest;
import com.carrental.model.CarType;
import com.carrental.model.Inventory;
import com.carrental.model.Reservation;
import lombok.Getter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Getter
@Service
public class ReservationService {
    // Expose inventory for tests
    private final Inventory inventory;

    public ReservationService() {
        // default capacities: Sedan 3, SUV 2, VAN 1
        this.inventory = new Inventory(Map.of(
                CarType.SEDAN, 3,
                CarType.SUV, 2,
                CarType.VAN, 1
        ));
    }

    // For tests to inject custom inventory
    public ReservationService(Inventory inventory) {
        this.inventory = inventory;
    }

    public Mono<Reservation> reserve(ReservationRequest req) {
        if (req == null || req.getCarType() == null || req.getStart() == null || req.getDays() <= 0) {
            return Mono.error(new IllegalArgumentException("Invalid reservation request"));
        }
        return Mono.fromCallable(() -> inventory.reserve(req.getCarType(), req.getStart(), req.getDays())
                .orElseThrow(() -> new IllegalStateException("No cars available for the requested type/time"))
        );
    }

}

