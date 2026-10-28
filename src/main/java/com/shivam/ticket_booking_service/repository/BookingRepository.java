package com.shivam.ticket_booking_service.repository;

import com.shivam.ticket_booking_service.entity.Booking;
import com.shivam.ticket_booking_service.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserOrderByCreatedAtDesc(User user);
}