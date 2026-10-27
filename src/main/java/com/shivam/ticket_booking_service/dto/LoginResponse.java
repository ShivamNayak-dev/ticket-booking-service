
package com.shivam.ticket_booking_service.dto;

public record LoginResponse(
    String message,
    Long userId,
    String name,
    String email,
    String role
) {}
