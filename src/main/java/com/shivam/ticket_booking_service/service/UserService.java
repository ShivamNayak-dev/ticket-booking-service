
package com.shivam.ticket_booking_service.service;

import com.shivam.ticket_booking_service.dto.RegisterRequest;
import com.shivam.ticket_booking_service.dto.RegisterResponse;
import com.shivam.ticket_booking_service.entity.Role;
import com.shivam.ticket_booking_service.entity.User;
import com.shivam.ticket_booking_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//new
import com.shivam.ticket_booking_service.dto.LoginRequest;
import com.shivam.ticket_booking_service.dto.LoginResponse;
import com.shivam.ticket_booking_service.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name()
                
        );
        
        
        
    }
    

public LoginResult login(LoginRequest request) {

    String email = request.email().trim().toLowerCase();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new BadCredentialsException("Invalid email or password"));

    if (!passwordEncoder.matches(
            request.password(), user.getPassword())) {
        throw new BadCredentialsException("Invalid email or password");
    }

    String token = jwtService.generateToken(
            user.getId(),
            user.getEmail(),
            user.getRole().name()
    );

    LoginResponse response = new LoginResponse(
            "Login successful",
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name()
    );

    return new LoginResult(token, response);
}

public record LoginResult(String token, LoginResponse response) {}

}
