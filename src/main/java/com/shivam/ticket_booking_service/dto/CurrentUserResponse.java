package com.shivam.ticket_booking_service.dto;

public record CurrentUserResponse(
        Long id,
        String name,
        String email,
        String role
) {}