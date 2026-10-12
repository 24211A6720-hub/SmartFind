# SmartFind - Viva Guide

## Architecture in one picture
React (pages, Axios) -> REST/JSON + JWT -> Spring Security filter -> Controller -> Service -> MatchStrategy -> Repository (JPA/Hibernate) -> MySQL

## Why these technologies?
- **Spring Boot**: auto-configuration, embedded Tomcat, huge ecosystem; one `mvn spring-boot:run` starts everything.
- **React**: component-based UI, fast updates without page reloads; one codebase for all pages.
- **MySQL**: relational data with relationships (users - reports - matches - claims) and constraints.
- **REST API**: frontend and backend are separate; they only exchange JSON over HTTP, so either can change independently.
- **JPA/Hibernate**: we work with Java objects; Hibernate generates SQL and creates tables.
- **JWT**: stateless login. The server does not store sessions; every request carries a signed token.

## How authentication works
1. `POST /api/auth/login` checks the email and compares the password with the BCrypt hash.
2. The server creates a JWT (email + role, signed with a secret, expires in 24h) and returns it.
3. React stores it in localStorage; the Axios interceptor adds `Authorization: Bearer <token>` to every request.
4. `JwtAuthFilter` validates the signature/expiry, loads the user and puts it in Spring's SecurityContext.
5. `SecurityConfig` rules decide access: `/api/admin/**` needs ROLE_ADMIN, everything except login/register needs a valid token.
6. Services add ownership rules (only the owner edits a report; only the lost-report owner claims).
BCrypt is a one-way hash with a random salt: passwords are never stored or recoverable.

## How the matching algorithm works
When a report is saved, `MatchingService.runFor()`:
1. Loads open reports of the opposite type (not the same user, found date not earlier than lost date - 1 day).
2. For each pair `WeightedMatchStrategy` computes six scores (0-100):
   - **Name 25%**: words are cleaned (lower-case, stop words removed, plurals trimmed, synonyms such as mobile=phone). Each word of the shorter name is matched to the closest word of the other (typos tolerated using Levenshtein distance). Score = 60% overlap + 40% Jaccard.
   - **Category 20%**: exact match = 100 else 0.
   - **Color 15%**: same/contained = 100, shared word = 80, same colour family (black/grey/silver) = 50, else 0.
   - **Location 15%**: word containment ("college library near Anurag University" contains "Anurag University").
   - **Date 10%**: within 1 day = 100, falls linearly to 0 at 14 days.
   - **Description 15%**: word overlap of description + identifying details.
3. Total = weighted average. >= 80 STRONG, >= 60 POSSIBLE, else WEAK. Pairs below 20 are not stored.
4. Reasons ("Same color", ...) are every component that scores high, so the result is explainable.
5. At STRONG, both owners get a notification and the reports become MATCHED.

Worked example (Black Leather Wallet vs Black Wallet): name 87, category 100, color 100, location 100, date 100, description 77
-> 87x0.25 + 100x0.20 + 100x0.15 + 100x0.15 + 100x0.10 + 77x0.15 = **93.3% STRONG**.

Why not ML? We have no labelled training data, and a rule-based system is transparent and testable. The `MatchStrategy` interface lets us plug in NLP later.

## Claims, notifications, admin
- **Claim**: only the owner of the lost report can claim a non-weak, still-active match. Status PENDING -> admin APPROVED/REJECTED.
- **Approval**: claim APPROVED, match CLAIMED, both reports CLAIMED, competing claims auto-rejected, other matches dismissed, contact details revealed. **Close case** sets everything CLOSED.
- **Notifications**: rows in the `notifications` table. The navbar asks `/api/notifications/unread-count` every 30 s. No email/SMS.
- **Admin**: statistics, user enable/disable, report moderation, claim review, match monitoring.

## Database (6 tables)
users, lost_items, found_items, match_results (unique lost+found pair), claims (FK match, claimant, reviewed_by), notifications (FK user, optional match).

## Frontend <-> backend <-> database
React page -> Axios call (`src/api/index.js`) -> `@RestController` -> `@Service` (business rules, `@Transactional`) -> `JpaRepository` -> Hibernate SQL -> MySQL. Responses use DTO records wrapped in `{success, message, data}`.

## Likely viva questions
1. **Is this AI?** No. It is a rule-based weighted similarity algorithm; it is explainable and can later be replaced by ML through `MatchStrategy`.
2. **Why DTOs instead of entities?** To avoid leaking fields (password hash), avoid lazy-loading/recursion problems and to control what each user may see (contact info hidden).
3. **Why is the table called match_results?** `MATCH` is a reserved word in MySQL.
4. **How do you stop someone claiming anything?** Only the lost-report owner can claim; an admin must approve; contact details stay hidden until approval.
5. **What if two people claim the same match?** Only the lost owner can claim; if multiple pending claims exist, approving one auto-rejects the others.
6. **How are passwords protected?** BCrypt hashing with salt; validation requires 8+ characters with letter and number.
7. **What does stateless mean?** The server keeps no session; the JWT carries identity, so any server instance can verify it.
8. **How do you handle errors?** `GlobalExceptionHandler` maps exceptions to HTTP codes (400, 401, 403, 404, 409) with a consistent JSON body; stack traces are only logged on the server.
9. **How would you improve matching?** Add NLP embeddings for descriptions, image similarity, geo-distance, and learn weights from confirmed matches.
10. **How do you test it?** Unit tests for the algorithm, an integration test of the full workflow on H2, MockMvc tests for the API, plus a Postman collection.
11. **What is CORS?** Browsers block calls between different origins (5173 -> 8080) unless the server allows them; configured in `SecurityConfig`.
12. **Why `ddl-auto=update`?** Convenient for development; for production use migrations (Flyway) instead.
13. **Limitations?** No image upload, notifications are polled not pushed, in-memory search filtering, rule-based matching only.
