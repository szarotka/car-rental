package com.carrental.repository;

import com.carrental.model.CarInventory;
import com.carrental.model.CarType;
import com.carrental.model.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ReservationRepository {

    private final List<Reservation> reservations = new ArrayList<>();

    private final CarRepository carRepository;
    private final Map<CarType, ReentrantLock> lockForCarType = new ConcurrentHashMap<>();

    public Optional<CarInventory> getCarInventoryForTypeIfAvailable(CarType carType, LocalDateTime start, int days) {
        return carRepository.getCars().stream()
                .filter(carInventory -> carInventory.type() == carType
                        && isAvailable(carInventory, start, days))
                .findFirst();
    }

    private boolean isAvailable(CarInventory carInventory, LocalDateTime start, int days) {
        return reservations.stream()
                .filter(reservation -> reservation.carType().equals(carInventory.type()) && overlaps(reservation.start(), reservation.days(), start, days))
                .count() < carInventory.numberOfCars();
    }

    private boolean overlaps(LocalDateTime start1, int days1, LocalDateTime start2, int days2) {
        LocalDateTime end1 = start1.plusDays(days1);
        LocalDateTime end2 = start2.plusDays(days2);
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public Mono<Reservation> createReservation(CarInventory carInventory, LocalDateTime start, int days, UUID customerId) {
        lockForCarType.computeIfAbsent(carInventory.type(), type -> new ReentrantLock());
        ReentrantLock lock = lockForCarType.get(carInventory.type());
        lock.lock();
        try {
            if (!isAvailable(carInventory, start, days)) {
                log.error("Attempted to create reservation for unavailable car: {}", carInventory.type());
                return Mono.error(new IllegalStateException("Car is not available for the requested time period"));
            }
            Reservation reservation = new Reservation(UUID.randomUUID(), carInventory.type(), start, days, customerId);
            addReservation(reservation);
            return Mono.just(reservation);
        } finally {
            lock.unlock();
            lockForCarType.remove(carInventory.type());
        }
    }

    private void addReservation(Reservation reservation) {
        reservations.add(reservation);
    }

}
