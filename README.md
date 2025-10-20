# Share-Ware

Expense tracking, Tab organizing and group sharing calculation application.

# Note

This branch is non-functional and provided for illustrative purposes only. It may contain incomplete features, placeholders, or mock implementations. It is not intended to be evaluated


## Overview

Share-Ware lets users create groups, add expenses, settle balances, and compute who owes whom. It provides JWT-based authentication, fine-grained resource authorization, event streaming for integrations, and pluggable locking and messaging backends.

## Modules

- data: JPA entities and repositories
- core: shared models/exceptions
- bus: pluggable messaging providers (Kafka, RabbitMQ)
- iam-service: user and access management (membership management) 
- user-service: user CRUD API
- group-service: group CRUD API
- expense-service: expense CRUD and settlement
- balance-service: balance computation and retrieval
- notification-service: event-driven notifications (placeholder)
- payment-service: payment processing (placeholder)

## Design Ideology
- Modular architecture with clear separation of concerns
- Domain Driven Design ensuring rich domain models
- Low coupling and high cohesion among services
- This design ensures scalability, maintainability, and extensibility
- This will also be stateless and container-friendly for cloud deployments
- IAM Service
  - Manages the authentication and authorization aspects
  - It will house the user roles and permissions
  - It will also have user-group and user-expense relationships
  - It will generate and validate JWT tokens
  - Whenever user logs in, the IAM service will issue a ID JWT token
  - This ID token will be exchanged for access tokens for other services
  - Access Request will have the resource entity required and IAM service will resolve the effective role for that resource
- Core
  - An Auth module that will have the common authentication and authorization logic
  - It will do the static verification of JWT tokens for scalability and quick access
  - It will be used by all services to validate the incoming requests and provide inhouse resource-based authorization logic
- User Service
  - This will have the user entities and user management APIs
  - This will be using RDBMS with master/slave setup for high availability
- Group Service
  - This will have the group entites and group management APIs
  - This will be using RDBMS with master/slave setup for high availability
- Expense Service
  - This will have the expense entities and expense management APIs
  - This will be using RDBMS with master/slave setup for high availability
- Balance Service
  - This will have balance computation logic and APIs
- Notification Service
  - This will integrate with external notification systems (email, SMS, push)
- Payment Service
  - This will integrate with external payment gateways (Stripe, PayPal)
- Pluggable components for locking and messaging
- Resource-based authorization for fine-grained access control
- Event-driven design for extensibility and integrations

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

