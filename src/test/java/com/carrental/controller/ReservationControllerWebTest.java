package com.carrental.controller;

import com.carrental.dto.ReservationRequest;
import com.carrental.dto.ReservationResponse;
import com.carrental.model.CarType;
import com.carrental.repository.CarRepository;
import com.carrental.repository.ReservationRepository;
import com.carrental.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReservationControllerWebTest {

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        CarRepository carRepository = new CarRepository();
        ReservationRepository reservationRepository = new ReservationRepository(carRepository);
        ReservationService service = new ReservationService(reservationRepository);
        ReservationController controller = new ReservationController(service);

        this.client = WebTestClient.bindToController(controller)
                .build();
    }

    @Test
    void reserveReturnsOkAndBody_whenCarAvailable() {
        ReservationRequest req = new ReservationRequest(CarType.SEDAN, LocalDateTime.now().plusDays(1), 2, UUID.randomUUID());

        client.post()
                .uri("/reservation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(ReservationResponse.class)
                .value(resp -> {
                    assertNotNull(resp);
                    assertEquals("SEDAN", resp.carType());
                    assertEquals(2, resp.days());
                });
    }

    @Test
    void secondReservationForSameCarAndTime_returnsBadRequest() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        ReservationRequest r1 = new ReservationRequest(CarType.SUV, start, 2, UUID.randomUUID());
        ReservationRequest r2 = new ReservationRequest(CarType.SUV, start, 2, UUID.randomUUID());
        ReservationRequest r3 = new ReservationRequest(CarType.SUV, start, 2, UUID.randomUUID());

        // first should succeed
        client.post().uri("/reservation").contentType(MediaType.APPLICATION_JSON).bodyValue(r1).exchange()
                .expectStatus().isOk();
        client.post().uri("/reservation").contentType(MediaType.APPLICATION_JSON).bodyValue(r2).exchange()
                .expectStatus().isOk();

        // third should fail (no available car)
        client.post().uri("/reservation").contentType(MediaType.APPLICATION_JSON).bodyValue(r3).exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void invalidRequest_daysZero_returnsBadRequest() {
        ReservationRequest req = new ReservationRequest(CarType.SEDAN, LocalDateTime.now().plusDays(1), 0, UUID.randomUUID());

        client.post().uri("/reservation").contentType(MediaType.APPLICATION_JSON).bodyValue(req).exchange()
                .expectStatus().isBadRequest();
    }
}
