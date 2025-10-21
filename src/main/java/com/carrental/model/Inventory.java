package com.carrental.model;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Inventory {
    private final Map<CarType, Integer> capacity = new EnumMap<>(CarType.class);
    private final Map<CarType, List<Reservation>> reservations = new ConcurrentHashMap<>();

    // Per-type locks to avoid race conditions in concurrent reservations
    private final Map<CarType, ReentrantLock> locks = new EnumMap<>(CarType.class);

    public Inventory(Map<CarType, Integer> initialCapacity) {
        for (CarType ct : CarType.values()) {
            capacity.put(ct, initialCapacity.getOrDefault(ct, 0));
            reservations.put(ct, new ArrayList<>());
            locks.put(ct, new ReentrantLock());
        }
    }

    public boolean isAvailable(CarType type, LocalDateTime start, int days) {
        int cap = capacity.getOrDefault(type, 0);
        if (cap <= 0) return false;
        LocalDateTime end = start.plusDays(days);
        List<Reservation> list = reservations.get(type);
        long overlapping = list.stream().filter(r -> overlaps(r.getStart(), r.getDays(), start, days)).count();
        return overlapping < cap;
    }

    public Optional<Reservation> reserve(CarType type, LocalDateTime start, int days) {
        ReentrantLock lock = locks.get(type);
        lock.lock();
        try {
            if (!isAvailable(type, start, days)) return Optional.empty();
            Reservation r = new Reservation(UUID.randomUUID(), type, start, days);
            reservations.get(type).add(r);
            return Optional.of(r);
        } finally {
            lock.unlock();
        }
    }

    // For testing convenience
    public void clear() {
        for (CarType ct : CarType.values()) {
            reservations.get(ct).clear();
        }
    }

    private boolean overlaps(LocalDateTime aStart, int aDays, LocalDateTime bStart, int bDays) {
        LocalDateTime aEnd = aStart.plusDays(aDays);
        LocalDateTime bEnd = bStart.plusDays(bDays);
        return !aEnd.isBefore(bStart) && !bEnd.isBefore(aStart);
    }

    public int getCapacity(CarType type) {
        return capacity.getOrDefault(type, 0);
    }

    public List<Reservation> getReservations(CarType type) {
        return Collections.unmodifiableList(reservations.get(type));
    }
}

