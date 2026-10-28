
package com.shivam.ticket_booking_service.service;

import com.shivam.ticket_booking_service.dto.BookingRequest;
import com.shivam.ticket_booking_service.dto.BookingResponse;
import com.shivam.ticket_booking_service.entity.Booking;
import com.shivam.ticket_booking_service.entity.BookingStatus;
import com.shivam.ticket_booking_service.entity.Event;
import com.shivam.ticket_booking_service.entity.User;
import com.shivam.ticket_booking_service.entity.WaitlistEntry;
import com.shivam.ticket_booking_service.entity.WaitlistStatus;
import com.shivam.ticket_booking_service.repository.BookingRepository;
import com.shivam.ticket_booking_service.repository.EventRepository;
import com.shivam.ticket_booking_service.repository.UserRepository;
import com.shivam.ticket_booking_service.repository.WaitlistRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final WaitlistRepository waitlistRepository;

    public BookingService(
            BookingRepository bookingRepository,
            EventRepository eventRepository,
            UserRepository userRepository,
            WaitlistRepository waitlistRepository) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.waitlistRepository = waitlistRepository;
    }

    @CacheEvict(
            cacheNames = {"eventDetails", "eventLists"},
            allEntries = true)
    @Transactional
    public BookingResponse createBooking(
            String email, BookingRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"));

        Event event = eventRepository.findByIdForUpdate(request.eventId());

        if (event == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Event not found");
        }

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This event has already started");
        }

        if (event.getAvailableSeats() < request.quantity()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Not enough seats available");
        }

        event.setAvailableSeats(
                event.getAvailableSeats() - request.quantity());

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
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"));

        return bookingRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @CacheEvict(
            cacheNames = {"eventDetails", "eventLists"},
            allEntries = true)
    @Transactional
    public BookingResponse cancelBooking(
            String email, Long bookingId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"));

        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot cancel another user's booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Booking is already cancelled");
        }

        // Lock the event before changing its available seat count.
        Event event = eventRepository.findByIdForUpdate(
                booking.getEvent().getId());

        if (event == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Event not found");
        }

        event.setAvailableSeats(
                event.getAvailableSeats() + booking.getQuantity());

        booking.setStatus(BookingStatus.CANCELLED);

        // Allocate newly available seats to eligible waitlisted users.
        promoteWaitlistedUsers(event);

        return toResponse(booking);
    }

    private void promoteWaitlistedUsers(Event event) {

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            return;
        }

        List<WaitlistEntry> waitingEntries =
                waitlistRepository
                        .findByEventAndStatusOrderByCreatedAtAscIdAsc(
                                event, WaitlistStatus.WAITING);

        for (WaitlistEntry entry : waitingEntries) {

            // Preserve FIFO order: stop if the oldest request cannot fit.
            if (event.getAvailableSeats() < entry.getQuantity()) {
                break;
            }

            event.setAvailableSeats(
                    event.getAvailableSeats() - entry.getQuantity());

            Booking promotedBooking = new Booking();
            promotedBooking.setUser(entry.getUser());
            promotedBooking.setEvent(event);
            promotedBooking.setQuantity(entry.getQuantity());
            promotedBooking.setStatus(BookingStatus.CONFIRMED);

            bookingRepository.save(promotedBooking);

            entry.setStatus(WaitlistStatus.BOOKED);
        }
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
