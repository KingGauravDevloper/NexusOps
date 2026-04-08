package com.nexusops.booking.kafka;

import com.nexusops.booking.entity.ReservationStatus;
import com.nexusops.booking.repository.ReservationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Component
public class BookingSagaConsumer {
    private final ReservationRepository reservationRepository;

    public BookingSagaConsumer(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    @KafkaListener(topics = "payment.success", groupId = "booking-group")
    public void handlePaymentSuccess(Map<String, Object> paymentEvent) {
        UUID resId = UUID.fromString(paymentEvent.get("reservationId").toString());
        reservationRepository.findById(resId).ifPresent(reservation -> {
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);
            System.out.println("Booking " + resId + " CONFIRMED across saga!");
        });
    }

    @Transactional
    @KafkaListener(topics = "payment.failed", groupId = "booking-group")
    public void handlePaymentFailed(Map<String, Object> paymentEvent) {
        UUID resId = UUID.fromString(paymentEvent.get("reservationId").toString());
        reservationRepository.findById(resId).ifPresent(reservation -> {
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
            System.out.println("Booking " + resId + " CANCELLED due to failed payment saga!");
        });
    }
}
