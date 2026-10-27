
package com.shivam.ticket_booking_service.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/api/test-protected")
    public Map<String, String> testProtected(Authentication authentication) {
        return Map.of(
                "message", "You are authenticated!",
                "email", authentication.getName()
        );
    }
}
