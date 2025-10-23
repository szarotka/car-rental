package com.carrental.service;

import com.carrental.dto.ReservationRequest;
import com.carrental.dto.ReservationResponse;
import com.carrental.model.CarInventory;
import com.carrental.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public Mono<ReservationResponse> reserve(ReservationRequest request) {
        if (request == null || request.carType() == null || request.start() == null || request.days() <= 0) {
            return Mono.error(new IllegalArgumentException("Invalid reservation request"));
        }

        return reservationRepository.getCarInventoryForTypeIfAvailable(request.carType(), request.start(), request.days())
                .map(carInventory -> reserve(carInventory, request))
                .orElse(Mono.error(new IllegalStateException("No cars available for the requested type/time")));
    }

    private Mono<ReservationResponse> reserve(CarInventory car, ReservationRequest request) {
        return reservationRepository.createReservation(car, request.start(), request.days(), request.customerId())
                .map(reservation -> new ReservationResponse(
                        reservation.id(),
                        reservation.carType().name(),
                        reservation.start(),
                        reservation.days()));
    }
}
