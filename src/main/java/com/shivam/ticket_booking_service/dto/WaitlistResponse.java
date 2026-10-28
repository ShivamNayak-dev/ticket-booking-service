
package com.shivam.ticket_booking_service.dto;

import com.shivam.ticket_booking_service.entity.WaitlistStatus;
import java.time.LocalDateTime;

public record WaitlistResponse(
    Long id,
    Long eventId,
    String eventTitle,
    Integer quantity,
    WaitlistStatus status,
    Integer position,
    LocalDateTime createdAt
) {}
