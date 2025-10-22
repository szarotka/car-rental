package com.carrental.repository;

import com.carrental.model.Car;
import com.carrental.model.CarType;
import com.carrental.model.Reservation;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class ReservationRepository {

    private final List<Reservation> reservations = new ArrayList<>();

    private final CarRepository carRepository;

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
    }

    public Optional<Car> findFirstAvailableCar(CarType carType, LocalDateTime start, int days) {
        return carRepository.getCars().stream()
                .filter(car -> car.type() == carType
                        && isAvailable(car, start, days))
                .findFirst();
    }

    private boolean isAvailable(Car car, LocalDateTime start, int days) {
        return reservations.stream()
                .filter(reservation -> reservation.carId().equals(car.id()))
                .noneMatch(reservation -> overlaps(reservation.start(), reservation.days(), start, days));
    }

    private boolean overlaps(LocalDateTime start1, int days1, LocalDateTime start2, int days2) {
        LocalDateTime end1 = start1.plusDays(days1);
        LocalDateTime end2 = start2.plusDays(days2);
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public Mono<Reservation> createReservation(Car car, LocalDateTime start, int days, UUID customerId) {
        Reservation reservation = new Reservation(java.util.UUID.randomUUID(), car.type(), start, days, car.id(), customerId);
        addReservation(reservation);
        return Mono.just(reservation);
    }
}
