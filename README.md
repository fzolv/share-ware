# Share-Ware

Expense tracking, Tab organizing and group sharing calculation application.

## Required Installations
- Java 21
- Maven 3.9.11
- Install the dependencies and update the environment variables to the versions recommended above

## Bootstrap Instructions
- Add the google client and google secret you got in the mail in the application.yml file under hull module
- Add your user email for direct admin user bootstrapping to field bootstrap.admin.emails
- Add the pem certificates under hull/src/main/resources/credentials/
- Run the run-share-ware-main script on terminal

## Running Instructions
- We are using h2 database for ease of testing and demo purpose. Please use http://localhost:8080/shareware/h2-console to access the database console
- For obtaining jwt for running shareware apis. Visit http://localhost:8080/shareware/oauth2/authorization/google to login via google oauth2
- The token will be returned in the response
- Use the token in Authorization header as Bearer token to access other apis
- You can use the admin issue token api to generate tokens for other users


## Overview

Share-Ware lets users create groups, add expenses, settle balances, and compute who owes whom. It provides JWT-based authentication, fine-grained resource authorization, event streaming for integrations, and pluggable locking and messaging backends.

## Modules

- data: JPA entities and repositories
- core: shared models/exceptions
- hull: main HTTP API (Spring Boot), security, services, orchestration
- bus: pluggable messaging providers (Kafka, RabbitMQ)

## Design Considerations
- Due to single connection nature of H2 database, I had to use a monolith application to design the solution.
- I have designed with a ddd centric nature in branch ddd-design where each module is a separate bounded context.
- I have made use of strategy design in case of splitting algorithms for extendibility
- I have kept the system extendible and made wide use of provider design to readily integrate with any resource
- Like for bus I can use kafka or rabbitmq and for locks I can use in-memory, database or distributed locks
- The balance computation is designed to be efficient and scalable using snapshotting and windowing techniques
- Security is a first class citizen with JWT RS256 and resource based authorization enforced at controller level
- Event streaming allows for future integrations like notifications, analytics etc
- I have also added provisioning for integration with Payment Gateways in future for settlements

## Key Entities (data module)

- UserEntity
- GroupEntity
- GroupMemberEntity (role: GROUP_ADMIN, GROUP_MEMBER)
- ExpenseEntity
  - fields: id, group, description, amount, paidBy, currency, splitType, createdAt, updatedAt
- ExpenseSplitEntity (per-user owed amount for an expense)
- SettlementEntity (fromUserId, toUserId, amount, createdAt)
- GroupBalanceEntity (balanceId, groupId, calcAt)
- GroupUserBalanceEntity (id, balanceId, borrowerId, lenderId, amount)

## Security and Authorization

- JWT RS256 (keys loaded from classpath `credentials/private_key.pem`, `credentials/public_key.pem`)
- OAuth2 login support
- Resource-based authorization:
  - `ResourceAuthorizationService` resolves effective role per resource type (USER, GROUP, EXPENSE, BALANCE, SETTLEMENT)
  - `@PreAuthorize` via `@resourceAuth.hasRole(...)`
  - USER endpoints enforce self-or-admin (MEMBER can only access own userId)
  - GROUP/EXPENSE/BALANCE/SETTLEMENT endpoints require group roles (GROUP_MEMBER/GROUP_ADMIN) or ADMIN

## Locking

- `LockManager` delegates to `AdaptableLockProvider`
- Providers:
  - InMemoryLockProvider (default)
  - DatabaseLockProvider (placeholder)
  - DistributedLockProvider (placeholder)
- Select via `shareware.locks.provider` (in-memory | database | distributed)
- Applied to:
  - ExpenseServiceImpl: updateExpense, deleteExpense (per-expense lock)
  - SettlementServiceImpl: settleExpense (per-expense lock)
  - BalanceServiceImpl: recalculateGroupBalances (per-group lock)

## Event Streaming (bus module)

- Abstraction: `MessageBus` (createTopic, subscribe, poll, publish)
- Providers configured by `shareware.bus.provider`: kafka | rabbit
  - KafkaMessageBus (AdminClient/Producer/Consumer)
  - RabbitMessageBus (RabbitTemplate, listener container)
- Events published from hull:
  - Topic `shareware.expense.events`: EXPENSE_CREATED, EXPENSE_UPDATED, EXPENSE_DELETED
  - Topic `shareware.settlement.events`: EXPENSE_SETTLED
  - Topic `shareware.group.events`: GROUP_MEMBER_INVITED
- Event payloads include `userIds` for notification targeting
- We can extend the streaming capablity for notification service based use cases
- Like user should receive notification when he is added to a group or when an expense is created in a group he is part of, etc

## Balance Computation (snapshots with calcAt)

- One `calcAt` per group in `GroupBalanceEntity`
- Windowing:
  - `lastCalcAt` = latest group balance time or 1961-01-01 if none
  - `targetCalcAt` = now minus 5 minutes (avoids in-flight transactions)
  - Skip recompute if `targetCalcAt <= lastCalcAt`
- Delta sources between (lastCalcAt, targetCalcAt]:
  - Expenses by `updatedAt` (fallback to `createdAt`)
  - Settlements by `createdAt`
- Result persistence:
  - Upsert `GroupBalanceEntity(calcAt = targetCalcAt)` per group
  - Bulk replace `GroupUserBalanceEntity` rows for that balanceId
- Reads:
  - `getGroupBalanceSheet(groupId)`: returns current from/to amounts (legacy balance view)
  - `getUserBalanceSheet(userId)`: recomputes all their groups if needed, then returns entries involving the user
- Recompute triggers:
  - After expense creates/edits/deletes
  - After settlements
  - Periodic scheduler
  - On-demand at read time (safe, idempotent)
- This design ensures a well calibrated balance sheet with minimal recomputation and consistent snapshots.
- Window function will help in scaling the balance computation for large number of users and expenses

## Controllers and Operations

- UserController (/api/v1/users)
  - POST create (ADMIN)
  - GET by id (self or ADMIN)
  - PUT update (self or ADMIN)
  - DELETE (ADMIN)
  - GET all (ADMIN)
  - GET by email (ADMIN)
- GroupController (/api/v1/groups)
  - POST create (MEMBER/ADMIN)
  - GET/PUT/DELETE by groupId (GROUP_MEMBER/GROUP_ADMIN/ADMIN as appropriate)
  - Members: add/remove/list (GROUP_ADMIN for mutations, GROUP_MEMBER+ for reads)
- ExpenseController (/api/v1/expenses)
  - POST create (GROUP_MEMBER+ on groupId)
  - GET/PUT/DELETE by expenseId (EXPENSE resource role via group membership)
  - GET by groupId (GROUP_MEMBER+)
- SettlementController (/api/v1/settlements)
  - POST /expense/{expenseId} (GROUP_MEMBER+)
- BalanceController (/api/v1/balances)
  - GET group/{groupId} (recompute-if-needed, then return entries)
  - GET user/{userId} (recompute all user groups, then return entries)
- AdminController (/api/v1/admin)
  - POST /token/{userId} issues a JWT (ADMIN)

## Configuration

- hull/src/main/resources/application.yml (snippets):
  - H2 datasource (dev)
  - OAuth2 client (Google) example
  - Security: server context-path `/shareware`
  - Logging file path
  - `user-service.url`
  - `bootstrap.admin.emails`
  - `shareware.locks.provider`: in-memory | database | distributed
- bus/src/main/resources/application.yml:
  - `shareware.bus.provider`: kafka | rabbit
  - Kafka: `kafka-bootstrap-servers`, `kafka-client-id`
  - Rabbit: `rabbit-host`, `rabbit-port`, `rabbit-username`, `rabbit-password`

## JWT Keys (RS256)

- Place PEM files in `hull/src/main/resources/credentials/`:
  - `private_key.pem` (PKCS#8)
  - `public_key.pem` (X.509)
- Dev fallback: if files are missing, an in-memory keypair is generated (for development only)

## Run

Prereqs: Java 21, Maven, Kafka/Rabbit (optional for events)

Build:

```bash
mvn -q -DskipTests package
```

Run hull:

```bash
mvn -q -pl hull -am spring-boot:run
```

Admin token example:

```bash
curl -X POST 'http://localhost:8080/shareware/api/v1/admin/token/{USER_UUID}?ttlSeconds=3600' \
  -H 'Authorization: Bearer {ADMIN_JWT}'
```

