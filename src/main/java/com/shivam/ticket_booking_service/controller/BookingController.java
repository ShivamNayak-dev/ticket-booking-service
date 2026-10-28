package com.shivam.ticket_booking_service.controller;

import com.shivam.ticket_booking_service.dto.BookingRequest;
import com.shivam.ticket_booking_service.dto.BookingResponse;
import com.shivam.ticket_booking_service.service.BookingService;

import jakarta.validation.Valid;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        return bookingService.createBooking(
                authentication.getName(), request);
    }

    @GetMapping("/my")
    public List<BookingResponse> getMyBookings(
            Authentication authentication) {
        return bookingService.getMyBookings(authentication.getName());
    }

    @PostMapping("/{bookingId}/cancel")
    public BookingResponse cancelBooking(
            @PathVariable Long bookingId,
            Authentication authentication) {
        return bookingService.cancelBooking(
                authentication.getName(), bookingId);
    }
}