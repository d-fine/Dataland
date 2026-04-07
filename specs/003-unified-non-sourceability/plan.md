# Implementation Plan: Unified Non-Sourceability Lifecycle

**Branch**: `003-unified-non-sourceability` | **Date**: 2026-04-07 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/003-unified-non-sourceability/spec.md`

## Summary

Implement a **unified non-sourceability evaluation lifecycle** across three microservices:
- **dataland-backend**: Authoritative REST API and entity storage for non-sourceability requests
- **dataland-qa-service**: QA review workflow and decision management
- **dataland-data-sourcing-service**: State machine integration to track dataset sourcing status

**Core Flows**:
1. **Start Request**: User submits POST /metadata/nonSourceable → backend stores NonSourceabilityInformation → publishes event → QA service creates review task & data-sourcing service transitions state to NonSourceableVerification
2. **QA Acceptance**: Reviewer submits decision → QA service publishes event → backend sets currentlyActive=true → data-sourcing service transitions to NonSourceable
3. **QA Rejection**: Reviewer submits rejection → QA service publishes event → backend sets qaStatus=Rejected (no activation) → data-sourcing service leaves in NonSourceableVerification for manual handling

**Technical Approach**: At-most-once RabbitMQ messaging with idempotent event handlers; Dataland's existing authentication, rate limiting, and error conventions; 30-second P99 latency SLA for QA review task visibility.

## Technical Context

**Language/Version**: Kotlin 1.9+ (microservices), Vue 3 (optional frontend updates)  
**Primary Dependencies**: Spring Boot 3.x, Spring Data JPA, RabbitMQ (messaging), OpenAPI 3.x (contracts)  
**Storage**: PostgreSQL (backend & QA service store, data-sourcing reads)  
**Testing**: JUnit 5, MockMvc (unit/integration), Testcontainers (Docker-based integration tests)  
**Target Platform**: Docker/Kubernetes (Linux)  
**Project Type**: Web service (REST API + async event consumers)  
**Performance Goals**: 99% of requests reflected across services within 60 seconds; QA task visibility within 30 seconds (P99)  
**Constraints**: At-most-once delivery semantics; idempotent event handlers; zero data loss on duplicate events  
**Scale/Scope**: 3 microservices, ~5 new API endpoints, 2 new data entities per service, 4 new RabbitMQ events, ~100-150 test cases

## Constitution Check

**Principle I: Contract-First Service Boundaries** ✅

- OpenAPI 3.x documentation for `GET /metadata/nonSourceable`, `POST /metadata/nonSourceable`, `HEAD /metadata/nonSourceable/{companyId}/{dataType}/{reportingPeriod}` (backend)
- OpenAPI 3.x documentation for `GET /nonSourceable`, `GET /nonSourceable/queue`, `POST /nonSourceable/{nonSourceabilityId}` (QA service)
- PATCH /data-sourcing/{dataSourcingId}/state (data-sourcing service)
- Message schemas for non-sourceability-created, non-sourceability-auto-accepted, QA-accepted, QA-rejected events (RabbitMQ)
- All consumer services identified and backward compatibility noted

**Principle II: Backward-Compatible Messaging** ✅

- New event types (non-sourceability-created, non-sourceability-auto-accepted, QA-accepted, QA-rejected) do not affect existing consumers
- New fields on existing message payloads are additive and optional
- Schema versioning documented in contracts/ folder

**Principle III: Microservice Autonomy** ✅

- Backend: Independently deployable; acts as event publisher and orchestrator
- QA Service: Independently deployable; consumes non-sourceability-created events, publishes QA-decision events
- Data-Sourcing Service: Independently deployable; consumes all lifecycle events, updates state machine
- All cross-service communication via RabbitMQ (async) except read-only queries

**Principle IV: Mandatory Test Coverage** ✅

- Unit tests for entity creation, state transitions, event publishing
- Integration tests for endpoint behavior (with/without bypassQa, duplicate detection, error cases)
- E2E tests for full request->accept/reject->state-transition workflows
- Idempotency tests: replay same event 3x, verify no duplicate records or state corruption
- 80% line coverage floor on changed modules

**Principle V: Minimal Dependencies and Reviewable Changes** ✅

- No new external dependencies added (use existing Spring, RabbitMQ, Data JPA infrastructure)
- Changes isolated to entities, controllers, services, event handlers, state machines
- Clear PR scope: separate commits for backend, QA service, data-sourcing service

## Project Structure

### Documentation (this feature)

```text
specs/003-unified-non-sourceability/
├── plan.md              # This file (implementation plan)
├── research.md          # Phase 0 (research findings, if needed)
├── data-model.md        # Phase 1 (data entities and relationships)
├── quickstart.md        # Phase 1 (developer onboarding guide)
├── contracts/           # Phase 1 (OpenAPI & message schemas)
│   ├── backend-api.yaml
│   ├── qa-service-api.yaml
│   ├── datasourcing-api.yaml
│   └── events-schema.yaml
├── checklists/
│   └── requirements.md
└── tasks.md             # Phase 2 (generated task list - created by /speckit.tasks)
```

### Source Code (Dataland repository)

```text
# Backend Service: dataland-backend/
dataland-backend/
├── src/main/kotlin/
│   └── com/d_fine/dataland/backend/
│       ├── metadata/
│       │   ├── nonsourceable/
│       │   │   ├── controller/
│       │   │   │   └── NonSourceableMetadataController.kt
│       │   │   ├── entity/
│       │   │   │   └── NonSourceabilityInformationEntity.kt
│       │   │   ├── repository/
│       │   │   │   └── NonSourceabilityInformationRepository.kt
│       │   │   ├── service/
│       │   │   │   ├── NonSourceableService.kt
│       │   │   │   └── NonSourceableEventPublisher.kt
│       │   │   └── model/
│       │   │       ├── NonSourceabilityInformation.kt (DTO)
│       │   │       └── NonSourceabilityRequest.kt
│       │   └── ... (existing sourceable code)
│       └── ... (existing controllers, services)
└── src/test/kotlin/
    └── com/d_fine/dataland/backend/metadata/nonsourceable/
        ├── controller/
        │   └── NonSourceableMetadataControllerTest.kt
        ├── service/
        │   └── NonSourceableServiceTest.kt
        └── integration/
            └── NonSourceableEndToEndTest.kt

# QA Service: dataland-qa-service/
dataland-qa-service/
├── src/main/kotlin/
│   └── com/d_fine/dataland/qas/
│       ├── nonsourceable/
│       │   ├── controller/
│       │   │   └── NonSourceableQaController.kt
│       │   ├── entity/
│       │   │   └── NonSourceableQaReviewInformationEntity.kt
│       │   ├── repository/
│       │   │   └── NonSourceableQaReviewRepository.kt
│       │   ├── service/
│       │   │   ├── NonSourceableQaService.kt
│       │   │   ├── NonSourceableQaEventPublisher.kt
│       │   │   └── NonSourceableQaEventListener.kt
│       │   └── model/
│       │       ├── NonSourceableQaReviewInformation.kt (DTO)
│       │       └── NonSourceableQaDecision.kt
│       └── ... (existing QA code)
└── src/test/kotlin/
    └── com/d_fine/dataland/qas/nonsourceable/
        ├── controller/
        │   └── NonSourceableQaControllerTest.kt
        ├── service/
        │   ├── NonSourceableQaServiceTest.kt
        │   └── NonSourceableQaEventListenerTest.kt
        └── integration/
            └── NonSourceableQaEndToEndTest.kt

# Data-Sourcing Service: dataland-data-sourcing-service/
dataland-data-sourcing-service/
├── src/main/kotlin/
│   └── com/d_fine/dataland/datasourcing/
│       ├── state/
│       │   ├── model/
│       │   │   └── DataSourcingState.kt (enum: includes NonSourceableVerification, NonSourceable)
│       │   ├── service/
│       │   │   ├── DataSourcingStateService.kt (state transitions)
│       │   │   └── NonSourceableEventListener.kt (consumes lifecycle events)
│       │   └── security/
│       │       └── DataSourcingStateSecurityService.kt (replaces canUserPatchState logic)
│       └── ... (existing sourcing code)
└── src/test/kotlin/
    └── com/d_fine/dataland/datasourcing/state/
        ├── service/
        │   ├── DataSourcingStateServiceTest.kt
        │   └── NonSourceableEventListenerTest.kt
        └── integration/
            └── DataSourcingStateEndToEndTest.kt
```

**Structure Decision**: Three Dataland microservices (backend, qa-service, data-sourcing) each with isolated domain folders (nonsourceable, nonsourceable) containing controllers, entities, repositories, services, models, and corresponding tests.

## Phase 0: Research

**Status**: ✅ Completed during Clarification Workflow (2026-04-07)

**Findings**:
- Event delivery: At-most-once via RabbitMQ (decision B)
- QA review latency SLA: 30 seconds P99 (decision A)
- Rate limiting: Inherit Dataland defaults (decision A)
- bypassQa intent: Primarily testing, sometimes emergency override with audit logging (decision D)
- Error responses: Inherit Dataland API conventions (decision B)

**No unknowns remain** – all clarifications resolved and integrated into spec.md.

## Phase 1: Design & Contracts

### 1.1 Data Model

**Backend Service (dataland-backend)**

```kotlin
// Entity: NonSourceabilityInformationEntity
@Entity
@Table(name = "non_sourceability_information")
data class NonSourceabilityInformationEntity(
    @Id
    val nonSourceabilityId: UUID = UUID.randomUUID(),
    val companyId: UUID,
    val dataType: String,
    val reportingPeriod: String, // e.g., "2024-12-31"
    val reason: String,
    val uploaderUserId: String,
    val uploadTime: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
    val qaStatus: NonSourceabilityQaStatus = NonSourceabilityQaStatus.PENDING,
    val currentlyActive: Boolean = false,
    val byPassQa: Boolean = false // For audit trail
)

enum class NonSourceabilityQaStatus {
    PENDING, ACCEPTED, REJECTED
}

// DTO: REST API response type
data class NonSourceabilityInformation(
    val nonSourceabilityId: UUID,
    val companyId: UUID,
    val dataType: String,
    val reportingPeriod: String,
    val reason: String,
    val uploaderUserId: String,
    val uploadTime: ZonedDateTime,
    val qaStatus: NonSourceabilityQaStatus,
    val currentlyActive: Boolean
)
```

**QA Service (dataland-qa-service)**

```kotlin
// Entity: NonSourceableQaReviewInformationEntity
@Entity
@Table(name = "non_sourceable_qa_review_information")
data class NonSourceableQaReviewInformationEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    val nonSourceabilityId: UUID, // FK to backend record
    val companyId: UUID,
    val dataType: String,
    val reportingPeriod: String,
    val reason: String,
    val uploaderUserId: String,
    val uploadTime: ZonedDateTime,
    val qaStatus: NonSourceabilityQaStatus = NonSourceabilityQaStatus.PENDING,
    val reviewerUserId: String? = null,
    val qaComment: String? = null,
    val reviewTimestamp: ZonedDateTime? = null
)

// DTO: QA review task
data class NonSourceableQaReviewInformation(
    val nonSourceabilityId: UUID,
    val companyId: UUID,
    val dataType: String,
    val reportingPeriod: String,
    val reason: String,
    val uploaderUserId: String,
    val uploadTime: ZonedDateTime,
    val qaStatus: NonSourceabilityQaStatus,
    val reviewerUserId: String?,
    val qaComment: String?
)

// Request model for QA decision submission
data class NonSourceableQaDecision(
    val qaStatus: NonSourceabilityQaStatus, // Accepted or Rejected
    val qaComment: String? = null
)
```

**Data-Sourcing Service (dataland-data-sourcing-service)**

```kotlin
// State enum: Add new states to existing DataSourcingState
enum class DataSourcingState {
    // Existing states...
    DATA_SOURCING,
    DATA_SOURCING_DONE,
    DOCUMENT_SOURCING,
    DOCUMENT_SOURCING_DONE,
    // NEW: Non-sourceability states
    NON_SOURCEABLE_VERIFICATION, // Pending QA decision
    NON_SOURCEABLE               // Confirmed non-sourceable
}

// Update existing DataSourcingEntity to include new state values
```

### 1.2 Contract Specifications

**Backend API Contract** (`contracts/backend-api.yaml`)

```yaml
openapi: 3.0.0
info:
  title: Dataland Backend Non-Sourceability API
  version: 1.0.0
paths:
  /metadata/nonSourceable:
    post:
      summary: Create a non-sourceability request
      requestBody:
        content:
          application/json:
            schema:
              type: object
              properties:
                companyId: { type: string, format: uuid }
                dataType: { type: string }
                reportingPeriod: { type: string }
                reason: { type: string }
                bypassQa: { type: boolean, default: false }
              required: [companyId, dataType, reportingPeriod, reason]
      responses:
        '201':
          description: Non-sourceability request created
          content:
            application/json:
              schema: { $ref: '#/components/schemas/NonSourceabilityInformation' }
        '400':
          description: Bad request (malformed payload)
        '403':
          description: Forbidden (bypassQa without admin role)
        '409':
          description: Conflict (duplicate active request for same dataset)
    get:
      summary: Query non-sourceability records
      parameters:
        - name: companyId
          in: query
          schema: { type: string, format: uuid }
        - name: dataType
          in: query
          schema: { type: string }
        - name: reportingPeriod
          in: query
          schema: { type: string }
        - name: qaStatus
          in: query
          schema: { type: string, enum: [PENDING, ACCEPTED, REJECTED] }
      responses:
        '200':
          description: List of matching records
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/NonSourceabilityInformation' }
  /metadata/nonSourceable/{companyId}/{dataType}/{reportingPeriod}:
    head:
      summary: Check if non-sourceability record exists
      parameters:
        - name: companyId
          in: path
          required: true
          schema: { type: string, format: uuid }
        - name: dataType
          in: path
          required: true
          schema: { type: string }
        - name: reportingPeriod
          in: path
          required: true
          schema: { type: string }
      responses:
        '200':
          description: Record exists and is currently active
        '204':
          description: Record does not exist or is not currently active

components:
  schemas:
    NonSourceabilityInformation:
      type: object
      properties:
        nonSourceabilityId: { type: string, format: uuid }
        companyId: { type: string, format: uuid }
        dataType: { type: string }
        reportingPeriod: { type: string }
        reason: { type: string }
        uploaderUserId: { type: string }
        uploadTime: { type: string, format: date-time }
        qaStatus: { type: string, enum: [PENDING, ACCEPTED, REJECTED] }
        currentlyActive: { type: boolean }
```

**QA Service API Contract** (`contracts/qa-service-api.yaml`)

```yaml
openapi: 3.0.0
info:
  title: Dataland QA Service Non-Sourceability API
  version: 1.0.0
paths:
  /nonSourceable:
    get:
      summary: Query non-sourceability QA reviews
      parameters:
        - name: companyId
          in: query
          schema: { type: string, format: uuid }
        - name: dataType
          in: query
          schema: { type: string }
        - name: qaStatus
          in: query
          schema: { type: string, enum: [PENDING, ACCEPTED, REJECTED] }
        - name: showOnlyActive
          in: query
          schema: { type: boolean, default: false }
        - name: chunkSize
          in: query
          schema: { type: integer, default: 50 }
        - name: chunkIndex
          in: query
          schema: { type: integer, default: 0 }
      responses:
        '200':
          description: List of QA review records
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/NonSourceableQaReviewInformation' }
  /nonSourceable/queue:
    get:
      summary: Get pending QA review tasks (queue view)
      responses:
        '200':
          description: Pending review tasks only (qaStatus=PENDING)
  /nonSourceable/{nonSourceabilityId}:
    post:
      summary: Submit QA decision (Accept or Reject)
      parameters:
        - name: nonSourceabilityId
          in: path
          required: true
          schema: { type: string, format: uuid }
      requestBody:
        content:
          application/json:
            schema:
              type: object
              properties:
                qaStatus: { type: string, enum: [ACCEPTED, REJECTED] }
                qaComment: { type: string }
              required: [qaStatus]
      responses:
        '200':
          description: QA decision recorded
          content:
            application/json:
              schema: { $ref: '#/components/schemas/NonSourceableQaReviewInformation' }
        '404':
          description: Review record not found

components:
  schemas:
    NonSourceableQaReviewInformation:
      type: object
      properties:
        nonSourceabilityId: { type: string, format: uuid }
        companyId: { type: string, format: uuid }
        dataType: { type: string }
        reportingPeriod: { type: string }
        reason: { type: string }
        uploaderUserId: { type: string }
        uploadTime: { type: string, format: date-time }
        qaStatus: { type: string, enum: [PENDING, ACCEPTED, REJECTED] }
        reviewerUserId: { type: string, nullable: true }
        qaComment: { type: string, nullable: true }
```

**RabbitMQ Event Schemas** (`contracts/events-schema.yaml`)

```yaml
components:
  schemas:
    NonSourceabilityCreatedEvent:
      type: object
      properties:
        eventId: { type: string, format: uuid }
        nonSourceabilityId: { type: string, format: uuid }
        companyId: { type: string, format: uuid }
        dataType: { type: string }
        reportingPeriod: { type: string }
        reason: { type: string }
        uploaderUserId: { type: string }
        uploadTime: { type: string, format: date-time }
        eventPublishedTime: { type: string, format: date-time }
      required:
        - eventId
        - nonSourceabilityId
        - companyId
        - dataType
        - reportingPeriod
      description: "Published by backend when bypassQa=false; triggers QA review task creation and data-sourcing state transition to NonSourceableVerification"

    NonSourceabilityAutoAcceptedEvent:
      type: object
      properties:
        eventId: { type: string, format: uuid }
        nonSourceabilityId: { type: string, format: uuid }
        companyId: { type: string, format: uuid }
        dataType: { type: string }
        reportingPeriod: { type: string }
        reason: { type: string }
        uploaderUserId: { type: string }
        uploadTime: { type: string, format: date-time }
        eventPublishedTime: { type: string, format: date-time }
      required:
        - eventId
        - nonSourceabilityId
        - companyId
        - dataType
        - reportingPeriod
      description: "Published by backend when bypassQa=true; skips QA service, triggers direct data-sourcing state transition to NonSourceable"

    QaAcceptedEvent:
      type: object
      properties:
        eventId: { type: string, format: uuid }
        nonSourceabilityId: { type: string, format: uuid }
        qaStatus: { type: string, enum: [ACCEPTED] }
        reviewerUserId: { type: string }
        qaComment: { type: string, nullable: true }
        decisionTime: { type: string, format: date-time }
        eventPublishedTime: { type: string, format: date-time }
      required:
        - eventId
        - nonSourceabilityId
        - qaStatus
      description: "Published by QA service when reviewer accepts; backend sets currentlyActive=true, data-sourcing transitions to NonSourceable"

    QaRejectedEvent:
      type: object
      properties:
        eventId: { type: string, format: uuid }
        nonSourceabilityId: { type: string, format: uuid }
        qaStatus: { type: string, enum: [REJECTED] }
        reviewerUserId: { type: string }
        qaComment: { type: string, nullable: true }
        decisionTime: { type: string, format: date-time }
        eventPublishedTime: { type: string, format: date-time }
      required:
        - eventId
        - nonSourceabilityId
        - qaStatus
      description: "Published by QA service when reviewer rejects; backend sets qaStatus=REJECTED (no activation), data-sourcing remains in NonSourceableVerification"
```

### 1.3 Developer Quickstart

**File**: `quickstart.md`

This will be generated as a separate artifact with:
- Local setup steps (Docker Compose for PostgreSQL, RabbitMQ)
- Running each service individually
- Testing the full workflow (create request → accept/reject → verify state)
- Key code locations for each service
- RabbitMQ queue naming and topology
- Integration test template example

### 1.4 Agent Context Update

The update-agent-context script will run to ensure Copilot context is updated with the new services, entities, and workflows.

---

## Phase 2: Task Generation

**Status**: Pending `/speckit.tasks` command

This plan feeds into task generation, which will create an ordered, dependency-aware task list in `tasks.md` covering:
- Backend entity & repository changes
- QA service entity & API changes
- Data-sourcing state additions
- Event publisher/consumer implementations
- Contract documentation (OpenAPI)
- Unit tests (all three services)
- Integration tests (service-to-service via RabbitMQ)
- E2E tests (full workflow)
- Documentation updates

---

## Complexity Tracking

**Constitution Compliance**: ✅ PASS

All Dataland constitution principles are satisfied:
- **Contract-First**: OpenAPI and RabbitMQ event schemas documented
- **Backward-Compatible**: New entities/events do not break existing consumers
- **Microservice Autonomy**: Three independent services with async messaging
- **Test Coverage**: Comprehensive unit, integration, and E2E test plan
- **Minimal Dependencies**: No new external libraries required

**Risk Assessment**: 🟢 LOW
- Well-defined cross-service contracts
- Async messaging reduces coupling
- Idempotency simplifies retry/recovery logic
- 30-sec latency SLA is achievable with modern messaging infrastructure

**Recommended Next Step**: `/speckit.tasks` to generate ordered task list

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
