
package com.shivam.ticket_booking_service.repository;

import com.shivam.ticket_booking_service.entity.Event;
import com.shivam.ticket_booking_service.entity.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCategory(EventCategory category);

    List<Event> findByEventDateBetween(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @Query("""
        SELECT e FROM Event e
        WHERE (:category IS NULL OR e.category = :category)
          AND (:startDate IS NULL OR e.eventDate >= :startDate)
          AND (:endDate IS NULL OR e.eventDate <= :endDate)
        ORDER BY e.eventDate ASC
        """)
    List<Event> searchEvents(
            @Param("category") EventCategory category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Event findByIdForUpdate(@Param("id") Long id);
}
