
package com.shivam.ticket_booking_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


@Bean
public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        com.shivam.ticket_booking_service.security.JwtAuthenticationFilter jwtAuthenticationFilter)
        throws Exception {

    http
        // Temporary while we build the API.
        // Cookie-based authentication needs CSRF protection before production.
        .csrf(csrf -> csrf.disable())

        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/api/auth/register",
                "/api/auth/login"
            ).permitAll()
            .anyRequest().authenticated()
        )

        .addFilterBefore(
            jwtAuthenticationFilter,
            org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
        );

    return http.build();
}

}
