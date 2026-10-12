# Ledger API

A REST API for transferring funds between accounts, built with Spring Boot and JPA. Supports account creation, balance-validated transfers, and ownership-based access control — fully tested and deployed.

**Live API:** https://wealthtech-ledger.onrender.com
*(No frontend — this is a backend-only API. Test it with Postman, curl, or PowerShell's `Invoke-WebRequest`. Note: the free hosting tier spins down after inactivity, so the first request after a while may take 30-60 seconds to respond.)*

## Tech Stack

- **Java 17**, **Spring Boot 4**
- **Spring Data JPA** / Hibernate
- **Spring Security** (HTTP Basic Auth)
- **H2** (in-memory database)
- **Gradle**
- **Docker** (multi-stage build, deployed on Render)
- **JUnit 5** + `TestRestTemplate` (integration tests)

## Features

- **Account management** — create accounts with an owner and starting balance
- **Fund transfers** — move money between two accounts in a single request
- **Balance validation** — transfers are rejected if the sender doesn't have sufficient funds, with no partial state changes
- **Ownership-based access control** — a transaction is only visible to the sender or receiver, never a third party
- **Role-based authorization** — only users with the `OWNER` role can create or view transactions
- **Full test coverage** of the success and rejection paths for fund transfers, plus access-control edge cases

## API Endpoints

| Method | Path | Description | Auth required |
|---|---|---|---|
| `POST` | `/accounts` | Create a new account (also registers a matching login user) | No |
| `GET` | `/accounts/{id}` | View an account (owner only) | Yes |
| `POST` | `/transactions` | Create a transfer between two accounts | Yes (`OWNER` role) |
| `GET` | `/transactions/{id}` | View a transaction (sender or receiver only) | Yes |
| `GET` | `/transactions` | List all transactions involving the authenticated user (paginated) | Yes |
| `DELETE` | `/transactions/{id}` | Delete a transaction (sender or receiver only) | Yes |

### Example: create an account

```
POST /accounts
Content-Type: application/json

{ "owner": "sarah1", "password": "abc123", "balance": 1000.00 }
```
→ `201 Created`, with a `Location` header pointing to the new account. This also registers `sarah1`/`abc123` as a login user, so the account can immediately authenticate on other endpoints.

### Example: create a transfer

```
POST /transactions
Content-Type: application/json
Authorization: Basic <credentials>

{
  "description": "lunch",
  "amount": 50.00,
  "fromAccountId": 1,
  "toAccountId": 2
}
```
→ `201 Created` on success, or `422 Unprocessable Content` if the sender's balance is insufficient.

## Design Decisions

- **Balance validation over full double-entry accounting.** The project deliberately scopes to validating and applying single balance transfers (reject if insufficient funds, debit/credit both sides atomically) rather than modeling a full double-entry ledger with separate debit/credit entries and a chart of accounts. That's a much larger domain — the goal here was a defensible, complete piece of realistic backend logic, not a from-scratch accounting system.
- **Unidirectional JPA relationships.** `Transaction` holds `@ManyToOne` references to `Account` (`fromAccount`/`toAccount`), with no back-reference from `Account` to its transactions. This keeps the model simpler; the relationship is fully functional without it, and a back-reference can be added later if needed.
- **Ownership-based access control lives in the query layer.** Rather than filtering in application code, access checks (`fromAccount.owner OR toAccount.owner`) are expressed directly in Spring Data JPA repository methods, keeping authorization logic close to the data.
- **Business logic in the controller, not a separate service layer.** Given the project's size, keeping logic directly in the controller avoided unnecessary layering. A larger version of this project would extract a service layer.

## Known Limitations / Future Improvements

- **In-memory H2 database** — data resets on every restart. A production version would use a persistent database (e.g. PostgreSQL or Oracle).
- **Arbitrary starting balance on account creation** — `POST /accounts` currently accepts any starting balance the client provides, for demo/testing convenience. A production system would start every account at zero and require balance to come from a verified funding action.
- **No concurrency protection** — two simultaneous transfers from the same account could both pass the balance check before either commits. Optimistic locking (`@Version`) would close this gap.
- **HTTP Basic Auth**, not token-based (JWT) authentication.
