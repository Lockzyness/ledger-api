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
- **JUnit** + `TestRestTemplate` (integration tests)

## Features

- **Account management** — create an account with a unique username and password; new accounts start with a zero balance
- **Fund transfers** — move money between two accounts in a single request
- **Balance validation** — transfers are rejected if the sender doesn't have sufficient funds, with no partial state changes
- **Input validation** — request bodies are validated with Jakarta Bean Validation (transfer amount required and positive, account IDs required, username and password required); transfers to the same account are also rejected. Invalid requests return `400 Bad Request`
- **Ownership-based access control** — a transaction is only visible to the sender or receiver, never a third party
- **Role-based authorization** — only users with the `OWNER` role can create or view transactions
- **Integration tests** covering successful and rejected transfers, input validation, and access-control edge cases

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

{ "owner": "sarah1", "password": "abc123" }
```
→ `201 Created`, with a `Location` header pointing to the new account. This also registers `sarah1`/`abc123` as a login user, so the account can immediately authenticate on other endpoints. New accounts start with a balance of `0`. Returns `400` if the owner or password is missing, or `422` if the username is already taken.

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
→ `201 Created` on success. Otherwise:
- `400 Bad Request` — amount missing, zero or negative, or both IDs are the same account
- `403 Forbidden` — you don't own the sending account
- `404 Not Found` — either account doesn't exist
- `422 Unprocessable Content` — the sender's balance is insufficient

## Try it

The live API is seeded with two demo accounts and one transaction (`Lunch`, 50.00 from account 1 to account 2). These credentials are for demo use only.

| Username | Password | Account ID | Starting balance |
|---|---|---|---|
| `sarah1` | `abc123` | 1 | 950.00 |
| `kumar2` | `qwe` | 2 | 550.00 |

There is no login page — authenticate with HTTP Basic Auth on each request. With `curl`:

```bash
# 1. View sarah1's account
curl -u sarah1:abc123 https://wealthtech-ledger.onrender.com/accounts/1

# 2. Transfer 50.00 from account 1 to account 2 (expect 201 Created)
curl -i -u sarah1:abc123 -X POST https://wealthtech-ledger.onrender.com/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"coffee","amount":50.00,"fromAccountId":1,"toAccountId":2}'

# 3. Check the receiving account as kumar2 (balance should now be 600)
curl -u kumar2:qwe https://wealthtech-ledger.onrender.com/accounts/2
```

Things to try next: a wrong password returns `401`; `sarah1` requesting `/accounts/2` returns `404` (accounts are owner-only); a transfer larger than the sender's balance returns `422`.

Notes:
- **Windows PowerShell:** use `curl.exe` (plain `curl` is an alias for a different command), put each command on one line, and escape the JSON quotes, e.g. `-d "{\"description\":\"coffee\",\"amount\":50.00,\"fromAccountId\":1,\"toAccountId\":2}"`.
- Data is stored in memory and resets whenever the service restarts. It is also a shared demo, so balances may differ from the table above if someone else has already tried it.

## Design Decisions

- **Balance validation over full double-entry accounting.** The project deliberately scopes to validating and applying single balance transfers (reject if insufficient funds, debit/credit both sides atomically) rather than modeling a full double-entry ledger with separate debit/credit entries and a chart of accounts. That's a much larger domain — the goal here was a defensible, complete piece of realistic backend logic, not a from-scratch accounting system.
- **Unidirectional JPA relationships.** `Transaction` holds `@ManyToOne` references to `Account` (`fromAccount`/`toAccount`), with no back-reference from `Account` to its transactions. This keeps the model simpler; the relationship is fully functional without it, and a back-reference can be added later if needed.
- **Ownership-based access control lives in the query layer.** Rather than filtering in application code, access checks (`fromAccount.owner OR toAccount.owner`) are expressed directly in Spring Data JPA repository methods, keeping authorization logic close to the data. Single-transaction lookups use an explicit JPQL query, because a derived method name like `findByIdAndFromAccountOwnerOrToAccountOwner` groups the conditions incorrectly (AND binds tighter than OR) and would let a user who received any transaction access others.
- **New accounts start at zero.** Clients can't choose their own starting balance. In a real system, money would only enter through a verified funding source (e.g. a payment gateway); this project doesn't model funding, so balances change only through transfers.
- **Business logic in the controller, not a separate service layer.** Given the project's size, keeping logic directly in the controller avoided unnecessary layering. A larger version of this project would extract a service layer.

## Known Limitations / Future Improvements

- **In-memory H2 database** — data resets on every restart. A production version would use a persistent database (e.g. PostgreSQL or Oracle).
- **No funding mechanism** — accounts start at zero and there is no deposit/top-up endpoint yet, so money can only move between accounts that already hold a balance.
- **No concurrency protection** — two simultaneous transfers from the same account could both pass the balance check before either commits. Optimistic locking (`@Version`) would close this gap.
- **HTTP Basic Auth**, not token-based (JWT) authentication.
- **Deleting a transaction doesn't reverse balances** — deletion removes the record only. A real ledger would forbid deletion and record a reversing transaction instead.
