package com.carrental.controller;

import com.carrental.dto.ReservationRequest;
import com.carrental.model.CarType;
import com.carrental.repository.CarRepository;
import com.carrental.repository.ReservationRepository;
import com.carrental.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationControllerConcurrentTest {

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
    void concurrentRequestsReserveSameVAN_shouldOnlyAllowOne() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        // Use an executor and a latch to start requests as close to simultaneously as possible
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            List<Future<Integer>> futures = new java.util.ArrayList<>();
            for (int i = 0; i < 10; i++) {
                futures.add(executor.submit(() -> {
                    // wait for the signal to start
                    startLatch.await();
                    ReservationRequest req = new ReservationRequest(CarType.VAN, start, 2, UUID.randomUUID());
                    try {
                        // Use a short timeout to avoid hanging tests
                        // perform exchange and get status synchronously
                        int status = client.post()
                                .uri("/reservation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(req)
                                .exchange()
                                .returnResult(Void.class)
                                .getStatus()
                                .value();
                        return status;
                    } catch (Exception e) {
                        // treat exceptions as failures (code 0)
                        return 0;
                    }
                }));
            }

            // start all tasks
            startLatch.countDown();

            // gather results
            List<Integer> results = new java.util.ArrayList<>();
            for (Future<Integer> f : futures) {
                try {
                    Integer s = f.get(10, TimeUnit.SECONDS);
                    results.add(s);
                } catch (Exception e) {
                    results.add(0);
                }
            }

            long ok = results.stream().filter(s -> s != null && s == 200).count();
            long bad = results.stream().filter(s -> s != null && s == 400).count();
            long other = results.stream().filter(s -> s == null || (s != 200 && s != 400)).count();

            // Exactly one should succeed and the rest should be bad requests
            assertEquals(1, ok, "Expected exactly one successful reservation (200); results=" + results);
            assertEquals(9, bad, "Expected nine failed reservations (400); results=" + results);
            assertEquals(0, other, "Expected no other status codes or exceptions; results=" + results);

        } finally {
            executor.shutdownNow();
        }
    }
}
