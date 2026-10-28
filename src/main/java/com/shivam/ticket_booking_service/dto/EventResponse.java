
package com.shivam.ticket_booking_service.dto;

import com.shivam.ticket_booking_service.entity.EventCategory;

import java.time.LocalDateTime;

public record EventResponse(
    Long id,
    String title,
    String description,
    EventCategory category,
    String venue,
    LocalDateTime eventDate,
    Integer totalSeats,
    Integer availableSeats,
    LocalDateTime createdAt
) {}
