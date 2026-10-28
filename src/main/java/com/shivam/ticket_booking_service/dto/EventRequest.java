
package com.shivam.ticket_booking_service.dto;

import com.shivam.ticket_booking_service.entity.EventCategory;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record EventRequest(

    @NotBlank(message = "Event title is required")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    String title,

    @NotBlank(message = "Event description is required")
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    String description,

    @NotNull(message = "Event category is required")
    EventCategory category,

    @NotBlank(message = "Venue is required")
    @Size(max = 200, message = "Venue cannot exceed 200 characters")
    String venue,

    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    LocalDateTime eventDate,

    @NotNull(message = "Total seats are required")
    @Min(value = 1, message = "An event must have at least one seat")
    @Max(value = 100000, message = "Total seats cannot exceed 100000")
    Integer totalSeats

) {}
