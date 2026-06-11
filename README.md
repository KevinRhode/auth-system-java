# auth-system-java

A production-deployed, full stack authentication and authorization system built from scratch — no Auth0, no Firebase, no Spring Security starters doing the thinking for me. The goal was to understand every layer of a modern auth flow well enough to defend each design decision.

**Live demo:** [auth-system-java.com](https://auth-system-java.com)

| Layer | Tech | Hosting |
|---|---|---|
| Frontend | Angular (standalone components, signals) | Vercel |
| Backend | Spring Boot, Java 21 | Render |
| Database | PostgreSQL | Neon |
| Email | Resend | — |

---

## Features

- **Full auth lifecycle** — register, login, logout, token refresh, with email verification required before login
- **JWT with refresh token rotation** — 15-minute access tokens, 7-day refresh tokens stored server-side as sessions
- **httpOnly cookie storage** — tokens never touch JavaScript; `SameSite=None; Secure` for the cross-origin Vercel ↔ Render deployment
- **Session management dashboard** — users see all active sessions (device, last active), can revoke any of them, with the current session highlighted
- **Role-based access control** — USER / MODERATOR / ADMIN with method-level security (`@EnableMethodSecurity`)
- **Organizations** — users can create companies and manage members with OWNER / ADMIN / MEMBER roles
- **Admin user management view** — protected route + protected API, not just a hidden link
- **Rate limiting** — Bucket4j buckets on login, register, and resend-verification
- **Structured error contract** — a single `ErrorCode` enum drives `ApiException` → `GlobalExceptionHandler` → JSON the frontend branches on, so HTTP status and error semantics are defined in one place
- **Scheduled cleanup** — expired sessions and tokens are purged on a schedule rather than left to rot

---

## Security design decisions

These are the tradeoffs I consider the most interesting part of the project.

### No user enumeration, anywhere

Most sites accept a small leak: registration returns "email already taken," which lets an attacker confirm which addresses have accounts. I implemented the strict alternative — **every public auth endpoint returns a byte-identical response whether or not the account exists.**

- **Registration** always returns `201 — "Check your email."` If the address is new, a verification email is sent. If an account already exists, the owner instead receives a *"someone tried to register with your email — did you mean to log in?"* notification. The attacker sees the same response either way; the legitimate owner gets a useful security signal.
- **Resend verification** always returns success. Internally it's a silent no-op for unknown or already-verified addresses.
- **Forgot password** (planned) will follow the same contract.

### Timing equalization

Identical response bodies aren't enough if the response *times* differ. BCrypt costs ~100–300ms, so a registration path that skips hashing when the account exists is measurably faster — the latency itself becomes the oracle.

- The exists-branch of registration burns an equivalent `BCrypt.encode()` before returning.
- Login fetches the user, and if none exists, runs `BCrypt.matches()` against a static dummy hash before returning the same generic `INVALID_CREDENTIALS` it returns for a wrong password. Unknown email and wrong password are indistinguishable in both body and time.

One deliberate exception: login returns `EMAIL_NOT_VERIFIED` for unverified accounts. This only fires *after* a correct password, so it reveals nothing to an attacker who doesn't already hold the credentials — and it's essential UX for a real user.

### Refresh token rotation that survives concurrent requests

Naive rotation breaks when a page reload fires several API calls at once: the first refresh rotates the token, the second arrives with the now-stale one, and the user gets logged out for doing nothing wrong. My implementation only rotates when the refresh token has **less than one day of life remaining**, which preserves the security benefit of rotation while making concurrent refreshes idempotent in the common case.

### Cookies over localStorage

Access and refresh tokens live in httpOnly cookies, making token theft via XSS structurally impossible rather than merely unlikely. The cost is real CSRF surface area and a cross-origin cookie configuration (`SameSite=None; Secure`, explicit CORS allow-list with credentials) that took genuine debugging to get right across Vercel and Render.

---

## Architecture notes

- **Backend:** layered `controller → service → repository`, with `CookieUtil` centralizing cookie construction and `JwtAuthFilter` handling per-request authentication. Entities use explicit `@Getter`/`@Setter` patterns where Lombok's `@Data` would create hashCode/toString hazards on bidirectional JPA relationships.
- **Frontend:** Angular standalone components with `inject()` over constructor injection, signals + `computed()` for auth state, an HTTP interceptor that transparently refreshes on 401, reactive forms with cross-field validators (e.g., password confirmation), and route guards mirroring the backend's role checks. Modern `@if`/`@for` control flow throughout.
- **Defense in depth:** every frontend guard has a backend counterpart. The Angular admin guard is UX; `@Secured`/method security is the actual control.

---

## Running locally

**Prerequisites:** Java 21, Node 20+, PostgreSQL (local or a free Neon instance), a Resend API key.

```bash
# Backend
cd backend
# configure src/main/resources/application.properties (or env vars):
#   spring.datasource.url / username / password
#   JWT secret, Resend API key, frontend origin for CORS
./mvnw spring-boot:run

# Frontend
cd frontend
npm install
ng serve
```

The app runs at `http://localhost:4200` against the API at `http://localhost:8080`. In local dev, cookies fall back to `SameSite=Lax` since both origins are localhost.

---

## Known limitations / future work

Naming these is part of the point:

- **Rate limit buckets are in-memory** — they reset on redeploy and wouldn't be shared across instances. Fine on single-instance Render; the upgrade path is Bucket4j's Redis/JCache backends.
- **`ddl-auto` is not production-grade schema management** — migrating to Flyway is on the list.
- **Forgot-password flow** — designed (same no-enumeration contract as registration) but not yet built.
- **Email verification via GET** — verification currently mutates state on a GET request for email-client compatibility; a POST-confirm landing page would be more correct.

---

## Author

**Kevin Rhode** — [github.com/KevinRhode](https://github.com/KevinRhode)