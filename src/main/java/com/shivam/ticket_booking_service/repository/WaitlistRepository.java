
package com.shivam.ticket_booking_service.repository;

import com.shivam.ticket_booking_service.entity.Event;
import com.shivam.ticket_booking_service.entity.User;
import com.shivam.ticket_booking_service.entity.WaitlistEntry;
import com.shivam.ticket_booking_service.entity.WaitlistStatus;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitlistRepository
        extends JpaRepository<WaitlistEntry, Long> {

    List<WaitlistEntry> findByEventAndStatusOrderByCreatedAtAscIdAsc(
            Event event, WaitlistStatus status);

    List<WaitlistEntry> findByUserOrderByCreatedAtDesc(User user);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WaitlistEntry w WHERE w.id = :id")
    java.util.Optional<WaitlistEntry> findByIdForUpdate(
            @Param("id") Long id);
}
