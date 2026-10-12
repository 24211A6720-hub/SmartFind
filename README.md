# SmartFind - Intelligent Lost & Found Matching System

Spring Boot + React + MySQL. Users report lost/found items, a **rule-based weighted similarity engine**
(not machine learning) scores every possible pair from 0-100, strong matches trigger in-app notifications,
the owner submits a claim and an admin verifies it.

```
smartfind/
├── backend/    Spring Boot 3.3 (Java 17, Maven)   -> http://localhost:8080
├── frontend/   React 18 + Vite + React Router     -> http://localhost:5173
├── postman/    SmartFind.postman_collection.json
└── docs/       VIVA_GUIDE.md
```

## 1. Prerequisites
Java 17+, Maven 3.9+ (or the IDE's Maven), Node.js 18+, MySQL 8 running locally.

## 2. Run the backend
1. Start MySQL. The database `smartfind` is created automatically.
2. Open `backend/src/main/resources/application.properties` and set `spring.datasource.username` / `password` for YOUR MySQL.
3. In `backend/` run:
   ```
   mvn spring-boot:run
   ```
   (or open `backend/` in IntelliJ/Eclipse and run `SmartFindApplication`).
4. Tables are created by Hibernate and sample data is inserted on first start. Look for the log line
   `SmartFind DEV sample data created`.

## 3. Run the frontend
```
cd frontend
npm install
npm run dev
```
Open http://localhost:5173. The API URL is in `frontend/.env` (`VITE_API_URL`).

## 4. Development accounts (CHANGE BEFORE ANY REAL USE)
| Role  | Email                | Password   |
|-------|----------------------|------------|
| ADMIN | admin@smartfind.com  | Admin@123  |
| USER  | alice@smartfind.com  | User@1234  |
| USER  | bob@smartfind.com    | User@1234  |
| USER  | carol@smartfind.com  | User@1234  |

Sample data (created by `config/DataSeeder.java`, only when the users table is empty):
| Lost (owner)                    | Found (owner)                      | Expected |
|---------------------------------|------------------------------------|----------|
| Black Leather Wallet (Alice)    | Black Wallet (Bob)                 | STRONG ~93% |
| Silver Samsung Phone (Carol)    | Grey Mobile Phone (Bob)            | POSSIBLE ~70% |
| Blue Umbrella (Alice)           | Black Umbrella (Carol)             | WEAK ~44% (admin only) |
| -                               | Red Water Bottle (Carol)           | no match |

To reset sample data: drop the database (`DROP DATABASE smartfind;`) and restart the backend.

## 5. Demonstration flow (works end to end)
1. Register **User A** (or use Alice) -> Report Lost Item: "Black Wallet", category Wallet, color Black.
2. Register **User B** in another browser/incognito window -> Report Found Item: "Black wallet found near library".
3. SmartFind scores the pair automatically. User A's bell shows a notification.
4. User A -> Possible Matches: sees the percentage, reasons and score breakdown.
5. User A -> "This is mine - submit claim" with a message.
6. Log in as admin -> Claims -> Review and approve.
7. Report statuses become CLAIMED; User A can now see the finder's contact details.
8. Admin -> Matches -> **Close case**: both reports become CLOSED (resolved).

## 6. Matching algorithm (summary)
Score = sum(component x weight) / sum(weights). Defaults: name 25, category 20, color 15, location 15, date 10, description 15.
Levels: >= 80 STRONG, >= 60 POSSIBLE, below WEAK. All weights and thresholds are in `application.properties` (`smartfind.match.*`).
The engine sits behind the `MatchStrategy` interface so an NLP/ML strategy can replace it later.

## 7. Tests
```
cd backend
mvn test
```
- `WeightedMatchStrategyTest` - unit tests for the algorithm (no database).
- `SmartFindFlowTest` - complete workflow at service level on in-memory H2.
- `AuthControllerTest` - register/login/401/409/validation through MockMvc.

## 8. Postman
Import `postman/SmartFind.postman_collection.json`. Run the requests top to bottom: the login requests
save `userToken`, `finderToken` and `adminToken` automatically, and later requests save ids.

## 9. Common errors
| Problem | Fix |
|---|---|
| `Access denied for user 'root'` | Fix username/password in `application.properties`. |
| `Communications link failure` | MySQL is not running or the port is not 3306. |
| Browser: CORS error | Frontend must run on http://localhost:5173 (or add your origin to `smartfind.cors.allowed-origins`). |
| `Unsupported class file major version` | Use Java 17 or newer. |
| Login returns "Cannot reach the server" | Backend not running, or wrong `VITE_API_URL`. |
| Sample data missing | It only loads when the `users` table is empty. |
| `Port 8080 already in use` | Stop the other app or set `server.port=8081` (and update `frontend/.env`). |

## 10. Notes and simplifications (be honest in your viva)
- Matching is rule-based text similarity, **not AI/ML**.
- "Optional image" is an image **URL** field (no file upload) to keep the project simple.
- A claim is linked to a match (which already identifies both reports) instead of a bare report id.
- Search filters are applied in memory after loading reports - fine for a college-scale dataset; use JPA Specifications/indexes if data grows.
- Items by the same user are never matched with each other.
- Contact info and identifying details are hidden from other users until an admin approves the claim.
