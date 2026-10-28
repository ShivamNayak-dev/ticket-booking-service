
package com.shivam.ticket_booking_service.controller;

import com.shivam.ticket_booking_service.dto.RegisterRequest;
import com.shivam.ticket_booking_service.dto.RegisterResponse;
import com.shivam.ticket_booking_service.dto.LoginRequest;
import com.shivam.ticket_booking_service.dto.LoginResponse;
import com.shivam.ticket_booking_service.service.UserService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

import com.shivam.ticket_booking_service.dto.CurrentUserResponse;
import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @Value("${app.jwt.cookie-name}")
    private String cookieName;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.jwt.secure:false}")
    private boolean secureCookie;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request) {

        return userService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        UserService.LoginResult result = userService.login(request);

        ResponseCookie cookie = ResponseCookie
                .from(cookieName, result.token())
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofMillis(expirationMs))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result.response());
    }

    @GetMapping("/me")
    public CurrentUserResponse getCurrentUser(
            Authentication authentication) {

        return userService.getCurrentUser(authentication.getName());
    }


@PostMapping("/logout")
public ResponseEntity<Map<String, String>> logout() {

    ResponseCookie cookie = ResponseCookie
            .from(cookieName, "")
            .httpOnly(true)
            .secure(secureCookie)
            .path("/")
            .sameSite("Lax")
            .maxAge(0)
            .build();

    return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(Map.of("message", "Logged out successfully"));
}

}
