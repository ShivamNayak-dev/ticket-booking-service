# Ticket Booking Service

Spring Boot REST API for authenticated event ticket booking, seat locking, Redis caching, and waitlists.

## Requirements

- JDK 21 or later
- PostgreSQL
- Redis
- Maven (or the included Maven Wrapper)

## Configuration

Configure `src/main/resources/application.properties` locally. Your existing local file is preserved unchanged in this archive and remains ignored by Git; do not commit it. Keep database credentials and the JWT signing secret out of version control. The application expects PostgreSQL and Redis connection settings, plus `app.jwt.secret`, `app.jwt.expiration-ms`, `app.jwt.cookie-name`, and `app.jwt.secure`.

The JWT secret must be a sufficiently long Base64-encoded key. For local HTTP development, `app.jwt.secure` is normally `false`; use secure cookies behind HTTPS in deployment.

Create the database (for example `ticket_booking_db`) in PostgreSQL, then run `database/schema.sql` if you want to create the schema manually. If Hibernate schema generation is enabled in your local configuration, follow the existing configuration and avoid applying a conflicting schema to an existing database.

## Run

From the directory containing `pom.xml`:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Or use `mvn clean test` and `mvn spring-boot:run` if Maven is installed. The API runs on port `8080` by default unless your local configuration changes it.

## Authentication and CSRF

1. `GET /api/auth/csrf` to obtain a CSRF token and cookie.
2. `POST /api/auth/register` to create a regular user.
3. `GET /api/auth/csrf` again if needed, then `POST /api/auth/login` with the CSRF cookie and `X-XSRF-TOKEN` header. Login sets the HttpOnly JWT cookie.
4. Send the cookies with authenticated requests. State-changing requests require the CSRF token header.
5. `GET /api/auth/me` returns the current user; `POST /api/auth/logout` clears the JWT cookie.

Registration always creates a `USER`. Event creation (`POST /api/events`) requires `ADMIN`. For local assessment testing, promote a specific account directly in PostgreSQL after registering it:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'your-email@example.com';
```

Sign in again after changing the role.

## Main endpoints

- `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/logout`, `GET /api/auth/csrf`
- `GET /api/events`, `GET /api/events/{id}`, `POST /api/events` (ADMIN only)
- `POST /api/bookings`, `GET /api/bookings/my`, `POST /api/bookings/{bookingId}/cancel`
- `POST /api/waitlist`, `GET /api/waitlist/my`, `POST /api/waitlist/{entryId}/cancel`

Event listing supports optional `category`, `startDate`, and `endDate` query parameters using ISO local date-time values, for example `2027-01-15T10:00:00`. Invalid date ranges return `400`.

## Booking and caching notes

Booking and cancellation operations use PostgreSQL pessimistic row locks on the event row to prevent overselling. Cancelling a booking restores seats and attempts to promote waitlisted entries in FIFO order. Event list/detail responses are cached in Redis with a five-minute TTL and the cache is evicted when events or seat availability change.

## Important

- Never commit `application.properties`, real credentials, or a real JWT secret.
- The included automated test currently checks application context startup; test the API flow with PostgreSQL and Redis running before submission.
