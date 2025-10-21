package com.carrental.service;

import com.carrental.dto.ReservationRequest;
import com.carrental.model.CarType;
import com.carrental.model.Inventory;
import com.carrental.model.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReservationServiceTest {
    private ReservationService service;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory(Map.of(
                CarType.SEDAN, 2,
                CarType.SUV, 1,
                CarType.VAN, 1
        ));
        service = new ReservationService(inventory);
    }

    @Test
    void reserveSucceedsWhenAvailable() {
        ReservationRequest req = new ReservationRequest(CarType.SEDAN, LocalDateTime.now().plusDays(1), 3);
        Reservation r = service.reserve(req).block();
        assertNotNull(r);
        assertEquals(CarType.SEDAN, r.getCarType());
    }

    @Test
    void cannotReserveBeyondCapacity() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        ReservationRequest r1 = new ReservationRequest(CarType.SUV, start, 2);
        ReservationRequest r2 = new ReservationRequest(CarType.SUV, start, 2);

        assertNotNull(service.reserve(r1).block());
        Exception ex = assertThrows(RuntimeException.class, () -> service.reserve(r2).block());
        assertTrue(ex.getMessage().contains("No cars available"));
    }

    @Test
    void overlappingReservationsCountAgainstCapacity() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        // two sedans capacity 2
        ReservationRequest r1 = new ReservationRequest(CarType.SEDAN, start, 3);
        ReservationRequest r2 = new ReservationRequest(CarType.SEDAN, start.plusDays(1), 2);
        ReservationRequest r3 = new ReservationRequest(CarType.SEDAN, start.plusDays(2), 1);

        assertNotNull(service.reserve(r1).block());
        assertNotNull(service.reserve(r2).block());
        // r3 overlaps with r1/r2 so should fail because capacity 2
        Exception ex = assertThrows(RuntimeException.class, () -> service.reserve(r3).block());
        assertTrue(ex.getMessage().contains("No cars available"));
    }

    @Test
    void differentTypesAreIndependent() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        ReservationRequest sedan = new ReservationRequest(CarType.SEDAN, start, 1);
        ReservationRequest suv = new ReservationRequest(CarType.SUV, start, 1);

        assertNotNull(service.reserve(sedan).block());
        assertNotNull(service.reserve(suv).block());
    }
}