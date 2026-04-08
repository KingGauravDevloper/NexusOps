package com.nexusops.booking.service;

import com.nexusops.booking.entity.Reservation;
import com.nexusops.booking.entity.ReservationStatus;
import com.nexusops.booking.repository.ReservationRepository;
import com.nexusops.booking.repository.ResourceRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class BookingService {
    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final RedissonClient redissonClient;
    private final com.nexusops.booking.kafka.BookingEventPublisher eventPublisher;

    public BookingService(ReservationRepository reservationRepository, ResourceRepository resourceRepository, RedissonClient redissonClient, com.nexusops.booking.kafka.BookingEventPublisher eventPublisher) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.redissonClient = redissonClient;
        this.eventPublisher = eventPublisher;
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Transactional
    public Reservation createReservation(UUID resourceId, String userEmail, LocalDateTime start, LocalDateTime end) {
        // Redlock Phase 3 logic
        RLock lock = redissonClient.getLock("booking_lock_" + resourceId.toString());
        boolean acquired = false;
        
        try {
            // Wait up to 3s to acquire lock, hold for 10s automatically
            acquired = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("System is currently busy. Please try again later.");
            }

            var resource = resourceRepository.findById(resourceId)
                    .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

            Reservation reservation = new Reservation();
            reservation.setResource(resource);
            reservation.setUserEmail(userEmail);
            reservation.setStartTime(start);
            reservation.setEndTime(end);
            reservation.setStatus(ReservationStatus.PENDING); // Saga pattern will update this later

            Reservation saved = reservationRepository.save(reservation);
            eventPublisher.publishBookingCreated(saved.getId(), userEmail);
            return saved;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Booking process was interrupted.");
        } finally {
            // Ensure lock is released even if exceptions occur
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
