
package com.shivam.ticket_booking_service.controller;

import com.shivam.ticket_booking_service.dto.WaitlistRequest;
import com.shivam.ticket_booking_service.dto.WaitlistResponse;
import com.shivam.ticket_booking_service.service.WaitlistService;

import jakarta.validation.Valid;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/waitlist")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WaitlistResponse joinWaitlist(
            @Valid @RequestBody WaitlistRequest request,
            Authentication authentication) {

        return waitlistService.joinWaitlist(
                authentication.getName(), request);
    }

    @GetMapping("/my")
    public List<WaitlistResponse> getMyWaitlist(
            Authentication authentication) {

        return waitlistService.getMyWaitlist(
                authentication.getName());
    }

    @PostMapping("/{entryId}/cancel")
    public WaitlistResponse cancelWaitlist(
            @PathVariable Long entryId,
            Authentication authentication) {

        return waitlistService.cancelWaitlist(
                authentication.getName(), entryId);
    }
}
