package com.carrental.service;

import com.carrental.dto.ReservationRequest;
import com.carrental.dto.ReservationResponse;
import com.carrental.model.CarType;
import com.carrental.repository.CarRepository;
import com.carrental.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReservationServiceTest {

    private ReservationService service;

    @BeforeEach
    void setUp() {
        CarRepository carRepository = new CarRepository();
        ReservationRepository reservationRepository = new ReservationRepository(carRepository);
        service = new ReservationService(reservationRepository);
    }

    @Test
    void reserveSucceedsWhenAvailable() {
        ReservationRequest req = new ReservationRequest(CarType.SEDAN, LocalDateTime.now().plusDays(1), 3, UUID.randomUUID());
        ReservationResponse resp = service.reserve(req).block();
        assertNotNull(resp);
        assertEquals("SEDAN", resp.carType());
        assertEquals(3, resp.days());
    }

    @Test
    void cannotReserveBeyondCapacity() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        ReservationRequest r1 = new ReservationRequest(CarType.SUV, start, 2, UUID.randomUUID());
        ReservationRequest r2 = new ReservationRequest(CarType.SUV, start, 2, UUID.randomUUID());
        ReservationRequest r3 = new ReservationRequest(CarType.SUV, start, 2, UUID.randomUUID());

        assertNotNull(service.reserve(r1).block());
        assertNotNull(service.reserve(r2).block());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.reserve(r3).block());
        assertTrue(ex.getMessage().contains("No cars available"));
    }

    @Test
    void overlappingReservationsCountAgainstCapacity() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        ReservationRequest r1 = new ReservationRequest(CarType.SEDAN, start, 3, UUID.randomUUID());
        ReservationRequest r2 = new ReservationRequest(CarType.SEDAN, start.plusDays(1), 2, UUID.randomUUID());

        assertNotNull(service.reserve(r1).block());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.reserve(r2).block());
        assertTrue(ex.getMessage().contains("No cars available"));
    }

    @Test
    void differentTypesAreIndependent() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        ReservationRequest sedan = new ReservationRequest(CarType.SEDAN, start, 1, UUID.randomUUID());
        ReservationRequest suv = new ReservationRequest(CarType.SUV, start, 1, UUID.randomUUID());

        ReservationResponse rSedan = service.reserve(sedan).block();
        ReservationResponse rSuv = service.reserve(suv).block();

        assertNotNull(rSedan);
        assertNotNull(rSuv);
        assertEquals("SEDAN", rSedan.carType());
        assertEquals("SUV", rSuv.carType());
    }
}

