package com.nexusops.booking.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class BookingEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BookingEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishBookingCreated(UUID reservationId, String userEmail) {
        Map<String, Object> event = new HashMap<>();
        event.put("reservationId", reservationId.toString());
        event.put("userEmail", userEmail);
        kafkaTemplate.send("booking.created", reservationId.toString(), event);
    }
}
