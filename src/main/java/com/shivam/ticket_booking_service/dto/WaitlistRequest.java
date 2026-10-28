
package com.shivam.ticket_booking_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WaitlistRequest(

    @NotNull(message = "Event ID is required")
    Long eventId,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "At least one ticket is required")
    @Max(value = 10, message = "You can request at most 10 tickets")
    Integer quantity

) {}
