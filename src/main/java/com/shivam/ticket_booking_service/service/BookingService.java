package com.shivam.ticket_booking_service.service;

import com.shivam.ticket_booking_service.dto.BookingRequest;
import com.shivam.ticket_booking_service.dto.BookingResponse;
import com.shivam.ticket_booking_service.entity.Booking;
import com.shivam.ticket_booking_service.entity.BookingStatus;
import com.shivam.ticket_booking_service.entity.Event;
import com.shivam.ticket_booking_service.entity.User;
import com.shivam.ticket_booking_service.repository.BookingRepository;
import com.shivam.ticket_booking_service.repository.EventRepository;
import com.shivam.ticket_booking_service.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public BookingService(
            BookingRepository bookingRepository,
            EventRepository eventRepository,
            UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookingResponse createBooking(String email, BookingRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        Event event = eventRepository.findByIdForUpdate(request.eventId());

        if (event == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Event not found");
        }

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "This event has already started");
        }

        if (event.getAvailableSeats() < request.quantity()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Not enough seats available");
        }

        event.setAvailableSeats(event.getAvailableSeats() - request.quantity());

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(event);
        booking.setQuantity(request.quantity());
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking saved = bookingRepository.save(booking);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        return bookingRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BookingResponse cancelBooking(String email, Long bookingId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You cannot cancel another user's booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Booking is already cancelled");
        }

        // Lock the event before restoring seats.
        Event event = eventRepository.findByIdForUpdate(booking.getEvent().getId());

        if (event == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Event not found");
        }

        event.setAvailableSeats(
                event.getAvailableSeats() + booking.getQuantity());

        booking.setStatus(BookingStatus.CANCELLED);

        return toResponse(booking);
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getEvent().getId(),
                booking.getEvent().getTitle(),
                booking.getQuantity(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }
}