
package com.shivam.ticket_booking_service.service;

import com.shivam.ticket_booking_service.dto.EventRequest;
import com.shivam.ticket_booking_service.dto.EventResponse;
import com.shivam.ticket_booking_service.entity.Event;
import com.shivam.ticket_booking_service.entity.EventCategory;
import com.shivam.ticket_booking_service.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDateTime;
import java.util.List;

import com.shivam.ticket_booking_service.exception.ResourceNotFoundException;
import com.shivam.ticket_booking_service.exception.InvalidRequestException;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @CacheEvict(cacheNames = {"eventDetails", "eventLists"}, allEntries = true)
    @Transactional
    public EventResponse createEvent(EventRequest request) {
        Event event = new Event();

        event.setTitle(request.title().trim());
        event.setDescription(request.description().trim());
        event.setCategory(request.category());
        event.setVenue(request.venue().trim());
        event.setEventDate(request.eventDate());
        event.setTotalSeats(request.totalSeats());
        event.setAvailableSeats(request.totalSeats());

        return toResponse(eventRepository.save(event));
    }
    
    
    @Cacheable(cacheNames = "eventLists", key = "'all'")
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Cacheable(cacheNames = "eventDetails", key = "#id")
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
        		.orElseThrow(() ->
                new ResourceNotFoundException("Event not found: " + id));

        return toResponse(event);
    }

    @Cacheable(
    	    cacheNames = "eventLists",
    	    key = "'search:' + #category + ':' + #startDate + ':' + #endDate"
    	)
    @Transactional(readOnly = true)
    public List<EventResponse> searchEvents(
            EventCategory category,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        if (startDate != null && endDate != null
                && startDate.isAfter(endDate)) {
        	throw new InvalidRequestException(
        	        "Start date must be before or equal to end date");
        }

        return eventRepository.searchEvents(category, startDate, endDate)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getCategory(),
                event.getVenue(),
                event.getEventDate(),
                event.getTotalSeats(),
                event.getAvailableSeats(),
                event.getCreatedAt()
        );
    }
}
