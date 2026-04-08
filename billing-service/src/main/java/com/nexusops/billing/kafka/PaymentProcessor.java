package com.nexusops.billing.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentProcessor {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentProcessor(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "booking.created", groupId = "billing-group")
    public void processPayment(Map<String, Object> bookingEvent) {
        System.out.println("Processing Payment for booking: " + bookingEvent);
        
        String reservationId = bookingEvent.get("reservationId").toString();
        // Simulating external payment gateway integration. 80% success pass rate.
        boolean paymentSuccess = Math.random() > 0.2;

        Map<String, Object> paymentResult = new HashMap<>();
        paymentResult.put("reservationId", reservationId);

        if (paymentSuccess) {
            paymentResult.put("status", "SUCCESS");
            kafkaTemplate.send("payment.success", reservationId, paymentResult);
            System.out.println("SUCCESS - payment processed for booking " + reservationId);
        } else {
            paymentResult.put("status", "FAILED");
            kafkaTemplate.send("payment.failed", reservationId, paymentResult);
            System.out.println("FAILED - payment declined for booking " + reservationId);
        }
    }
}
