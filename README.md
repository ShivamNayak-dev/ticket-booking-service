# Ticket Booking Service

A small full-stack event ticketing application built for the Java Development assessment.

## Stack

- Java 17, Spring Boot, Spring Security, JWT in HttpOnly cookies
- PostgreSQL for users, events, tickets, and waitlist entries
- Redis for event caching and short-lived seat holds
- React + Vite for the browser client

## Run locally

1. Install Java 17+, Maven, Node.js 18+, and Docker Desktop.
2. From the project root, start PostgreSQL and Redis:

   ```sh
   docker compose up -d
   ```

3. In a terminal, start the API:

   ```sh
   cd backend
   mvn spring-boot:run
   ```

   The API runs at `http://localhost:8080`. Local defaults are provided by
   `backend/src/main/resources/application.properties`; that file is ignored by
   Git as requested. For deployment, set the variables listed in
   `backend/.env.example` in your runtime environment (Spring Boot does not load
   `.env` files by itself).

4. In another terminal, start the web client:

   ```sh
   cd frontend
   npm install
   npm run dev
   ```

   Open `http://localhost:5173`.

## Main endpoints

- `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me`
- `GET /api/events`, `GET /api/events/{id}`, `POST /api/events` (authenticated)
- `POST /api/events/{eventId}/bookings`, `GET /api/bookings/mine`
- `POST /api/events/{eventId}/waitlist`, `GET /api/events/{eventId}/waitlist/mine`

Event listing accepts `category`, `from`, and `to` query parameters. Bookings
are serialized with a PostgreSQL pessimistic event lock and protected by a
unique `(event_id, seat_number)` constraint. Redis holds short-lived seat
reservations while a booking is being committed.

## Database

The schema is maintained by Flyway in `backend/src/main/resources/db/migration`.
An equivalent PostgreSQL schema is provided at `database/schema.sql` for review
or manual setup. The default local profile lets Hibernate create/update tables;
Flyway is the source of truth for production migrations.

## Notes

This is an assessment-sized demo: payments, email delivery, and production
operations are intentionally outside its scope. JWTs are stored only in an
HttpOnly, SameSite cookie; set `app.auth.cookie-secure=true` behind HTTPS.
