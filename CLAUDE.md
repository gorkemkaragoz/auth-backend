# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
mvn clean package

# Run locally (requires local MySQL on port 3306)
./mvnw spring-boot:run

# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=AuthServiceImplTest

# Start full stack (MySQL + backend + frontend)
docker-compose up -d --build
```

For local development, use `--spring.profiles.active=dev` to load `application-dev.properties` (connects to localhost MySQL instead of Docker's `db` host).

## Architecture

Spring Boot 3.x REST API with JWT-based stateless authentication. Java 21 with Lombok and Records.

**Layers:**
- `controllers/` — REST endpoints (`AuthController`, `UserController`)
- `services/` — Business logic via interface + impl pattern (`AuthServiceImpl`, `UserServiceImpl`, `MailServiceImpl`)
- `repos/` — Spring Data JPA (`UserRepository` with `findByEmail`, `existsByEmail`)
- `security/` — `JwtService` (token gen/validation), `JwtAuthFilter` (per-request filter), `CustomUserDetailsService` (loads user by email)
- `config/` — `SecurityConfig` (Spring Security filter chain, CORS, route authorization)
- `dto/` — Java 21 Records for all request/response objects
- `entities/` — `User` JPA entity, `Role` enum (`ROLE_ADMIN`, `ROLE_MEMBER`)

**Request flow:** HTTP request → `JwtAuthFilter` → `SecurityConfig` route check → Controller → Service → Repository

## API Surface

Public routes (no auth):
- `POST /auth/register`, `POST /auth/login`, `POST /auth/forgot-password`, `POST /auth/reset-password`

Protected routes (requires `ROLE_ADMIN` JWT):
- `GET|POST /users`, `GET|PUT|DELETE /users/{userId}`

CORS is configured for `http://localhost:5173`.

## Key Implementation Details

- **JWT:** JJWT 0.11.5, 1-hour expiry, secret from `${JWT_SECRET_KEY}` env var. Role stored as a claim in the token.
- **Password reset:** 6-digit OTP sent via Gmail SMTP, expires in 5 minutes, stored on the `User` entity.
- **Password encoding:** BCrypt via Spring Security's `PasswordEncoder`.
- **DDL:** Hibernate `ddl-auto=update` — schema is managed automatically.

## Environment Variables

Required at runtime (set in `.env` for Docker Compose, or as system env vars locally):

| Variable | Purpose |
|---|---|
| `DB_PASSWORD` | MySQL root password |
| `JWT_SECRET_KEY` | HMAC secret for JWT signing |
| `GMAIL_USER` | Gmail address for OTP emails |
| `GMAIL_APP_PASS` | Gmail app-specific password |

## Testing

Unit tests use `@ExtendWith(MockitoExtension.class)` with `@Mock`/`@InjectMocks` — no Spring context loaded. Controller tests use `@WebMvcTest`. Assertions use AssertJ (`assertThat`, `assertThatThrownBy`).