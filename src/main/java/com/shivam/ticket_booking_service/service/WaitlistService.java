
package com.shivam.ticket_booking_service.service;

import com.shivam.ticket_booking_service.dto.WaitlistRequest;
import com.shivam.ticket_booking_service.dto.WaitlistResponse;
import com.shivam.ticket_booking_service.entity.Event;
import com.shivam.ticket_booking_service.entity.User;
import com.shivam.ticket_booking_service.entity.WaitlistEntry;
import com.shivam.ticket_booking_service.entity.WaitlistStatus;
import com.shivam.ticket_booking_service.repository.EventRepository;
import com.shivam.ticket_booking_service.repository.UserRepository;
import com.shivam.ticket_booking_service.repository.WaitlistRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public WaitlistService(
            WaitlistRepository waitlistRepository,
            EventRepository eventRepository,
            UserRepository userRepository) {
        this.waitlistRepository = waitlistRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public WaitlistResponse joinWaitlist(
            String email, WaitlistRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        Event event = eventRepository.findByIdForUpdate(request.eventId());

        if (event == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Event not found");
        }

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Event has already started");
        }

        if (event.getAvailableSeats() >= request.quantity()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Enough seats are available. Please book directly.");
        }

        List<WaitlistEntry> existingEntries =
                waitlistRepository
                        .findByEventAndStatusOrderByCreatedAtAscIdAsc(
                                event, WaitlistStatus.WAITING);

        boolean alreadyWaiting = existingEntries.stream()
                .anyMatch(entry ->
                        entry.getUser().getId().equals(user.getId()));

        if (alreadyWaiting) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You already have an active waitlist entry for this event");
        }

        WaitlistEntry entry = new WaitlistEntry();
        entry.setUser(user);
        entry.setEvent(event);
        entry.setQuantity(request.quantity());
        entry.setStatus(WaitlistStatus.WAITING);

        WaitlistEntry saved = waitlistRepository.save(entry);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WaitlistResponse> getMyWaitlist(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        return waitlistRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public WaitlistResponse cancelWaitlist(
            String email, Long entryId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        WaitlistEntry entry = waitlistRepository
                .findByIdForUpdate(entryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Waitlist entry not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot cancel another user's waitlist entry");
        }

        if (entry.getStatus() != WaitlistStatus.WAITING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This entry is no longer waiting");
        }

        entry.setStatus(WaitlistStatus.CANCELLED);

        return toResponse(entry);
    }

    private WaitlistResponse toResponse(WaitlistEntry entry) {

        Integer position = null;

        if (entry.getStatus() == WaitlistStatus.WAITING) {
            List<WaitlistEntry> waitingEntries =
                    waitlistRepository
                            .findByEventAndStatusOrderByCreatedAtAscIdAsc(
                                    entry.getEvent(),
                                    WaitlistStatus.WAITING);

            for (int i = 0; i < waitingEntries.size(); i++) {
                if (waitingEntries.get(i).getId().equals(entry.getId())) {
                    position = i + 1;
                    break;
                }
            }
        }

        return new WaitlistResponse(
                entry.getId(),
                entry.getEvent().getId(),
                entry.getEvent().getTitle(),
                entry.getQuantity(),
                entry.getStatus(),
                position,
                entry.getCreatedAt()
        );
    }
}
