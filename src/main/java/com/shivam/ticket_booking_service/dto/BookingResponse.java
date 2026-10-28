package com.shivam.ticket_booking_service.dto;

import com.shivam.ticket_booking_service.entity.BookingStatus;
import java.time.LocalDateTime;

public record BookingResponse(
    Long id,
    Long eventId,
    String eventTitle,
    Integer quantity,
    BookingStatus status,
    LocalDateTime createdAt
) {}