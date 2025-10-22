package com.carrental.service;

import com.carrental.dto.ReservationRequest;
import com.carrental.dto.ReservationResponse;
import com.carrental.model.Car;
import com.carrental.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public Mono<ReservationResponse> reserve(ReservationRequest request) {
        if (request == null || request.carType() == null || request.start() == null || request.days() <= 0) {
            return Mono.error(new IllegalArgumentException("Invalid reservation request"));
        }
        return reservationRepository.findFirstAvailableCar(request.carType(), request.start(), request.days())
                .map(car -> reserve(car, request))
                .orElse(Mono.error(new IllegalStateException("No cars available for the requested type/time")));
    }

    private Mono<ReservationResponse> reserve(Car car, ReservationRequest request) {
        return reservationRepository.createReservation(car, request.start(), request.days(), request.customerId())
                .map(reservation -> new ReservationResponse(
                        reservation.id(),
                        reservation.carType().name(),
                        reservation.start(),
                        reservation.days()));
    }
}
