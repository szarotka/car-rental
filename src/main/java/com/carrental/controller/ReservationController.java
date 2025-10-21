package com.carrental.controller;

import com.carrental.dto.ReservationRequest;
import com.carrental.dto.ReservationResponse;
import com.carrental.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping
    public Mono<ResponseEntity<ReservationResponse>> reserve(@RequestBody ReservationRequest req) {
        return service.reserve(req)
                .map(r -> ResponseEntity.ok(new ReservationResponse(r.getId(), r.getCarType().name(), r.getStart(), r.getDays())))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
    }
}

