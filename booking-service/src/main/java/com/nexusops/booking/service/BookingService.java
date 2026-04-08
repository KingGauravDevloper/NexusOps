package com.nexusops.booking.service;

import com.nexusops.booking.entity.Reservation;
import com.nexusops.booking.entity.ReservationStatus;
import com.nexusops.booking.repository.ReservationRepository;
import com.nexusops.booking.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {
    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;

    public BookingService(ReservationRepository reservationRepository, ResourceRepository resourceRepository) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Transactional
    public Reservation createReservation(UUID resourceId, String userEmail, LocalDateTime start, LocalDateTime end) {
        var resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        Reservation reservation = new Reservation();
        reservation.setResource(resource);
        reservation.setUserEmail(userEmail);
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setStatus(ReservationStatus.PENDING); // Saga pattern will update this later

        // TODO: Redis Redlock logic to prevent double-booking (Phase 3)
        // TODO: Fire Kafka event `booking.created` to Billing Service (Phase 4)

        return reservationRepository.save(reservation);
    }
}
