package com.nexusops.booking.controller;

import com.nexusops.booking.entity.Reservation;
import com.nexusops.booking.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<Reservation> getAll() {
        return bookingService.getAllReservations();
    }

    @PostMapping
    public ResponseEntity<Reservation> create(@RequestBody CreateBookingRequest request) {
        Reservation res = bookingService.createReservation(
            request.resourceId(), request.userEmail(), request.startTime(), request.endTime()
        );
        return ResponseEntity.ok(res);
    }
}

record CreateBookingRequest(UUID resourceId, String userEmail, LocalDateTime startTime, LocalDateTime endTime) {}
