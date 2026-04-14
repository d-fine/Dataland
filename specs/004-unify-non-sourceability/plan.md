# Implementation Plan: Unified Non-Sourceability Lifecycle

**Branch**: `004-unify-non-sourceability` | **Date**: 2026-04-08 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/004-unify-non-sourceability/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Rewire active backend non-sourceability persistence and APIs to a unified
non-sourceability workflow where `dataland-backend` is authoritative,
`dataland-qa-service` manages QA review lifecycle for non-sourceability, and
`dataland-data-sourcing-service` applies state transitions from backend/QA
events. The feature introduces new backend and QA entities, rewires existing
non-sourceable endpoints, formalizes dedicated bypass event semantics, enforces
at-least-once messaging with idempotent consumers, and keeps
`SourceabilityEntity` as backup-only data.

## Technical Context

**Language/Version**: Kotlin on Java 21 (Spring Boot services)  
**Primary Dependencies**: Spring Boot Web/Security/Data JPA, OpenAPI 3 annotations, RabbitMQ via shared message queue utilities, Flyway migrations  
**Storage**: PostgreSQL across backend, QA service, and data sourcing service  
**Testing**: Gradle test suites (Kotlin unit + integration tests), repository/service tests, API/controller tests where behavior changes  
**Target Platform**: Linux containerized backend services in Docker Compose/Kubernetes-like deployment
**OpenAPI Tooling**: `org.springdoc.openapi-gradle-plugin`, `libs.springdoc.openapi.ui`, and `openApi {}` blocks are already configured in `dataland-backend`, `dataland-qa-service`, and `dataland-data-sourcing-service`; no extra version pinning is required for this feature  
**Project Type**: Multi-service backend monorepo (event-driven web services)  
**Performance Goals**: Non-sourceability propagation reflected in consumer services within 60 seconds of event emission  
**Constraints**: At-least-once delivery with idempotent consumers, backward-compatible contracts, minimum 80% line coverage in changed modules, role-based authorization on bypass/admin state transitions  
**Scale/Scope**: Changes span three services (`dataland-backend`, `dataland-qa-service`, `dataland-data-sourcing-service`) plus shared messaging/API contracts

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Contract-first impact is documented: OpenAPI 3.x and/or message schema
  changes identify consumers, compatibility approach, and rollback strategy.
- Messaging compatibility is explicit: additive evolution by default;
  non-additive changes include coordinated rollout and migration notes.
- Test strategy is mandatory and concrete: Kotlin unit + integration tests,
  Vue unit/component tests, and e2e coverage for impacted critical journeys.
- Coverage gate is declared: changed modules remain at or above 80% line
  coverage and do not reduce existing baselines.
- Operational clarity is covered: correlation identifiers, fail-fast
  validation, and observability expectations are defined for async flows.
- Dependency and duplication controls are covered: no new dependency without
  documented necessity; duplication avoided or explicitly justified.

Status before Phase 0: PASS

- Contract-first impact: PASS. Endpoint rewiring and new event contracts are
  documented in `contracts/` with consumer mapping and compatibility guidance.
- Messaging compatibility: PASS. Existing non-sourceability-created and QA
  events remain additive; one new explicit bypass event is introduced with
  additive schema and coordinated consumer updates.
- Test strategy: PASS. Plan defines behavior tests for backend metadata APIs,
  QA non-sourceability APIs/listeners, and data-sourcing listener/state
  transitions.
- Coverage gate: PASS. No exceptions requested; changed modules must keep
  80%+ line coverage.
- Operational clarity: PASS. Correlation identifier requirements and fail-fast
  listener validation are included for all async flows.
- Dependency/duplication: PASS. No new third-party libraries planned; leverage
  existing converters, queue utilities, and OpenAPI patterns.

## Project Structure

### Documentation (this feature)

```text
specs/004-unify-non-sourceability/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
dataland-backend/
├── src/main/kotlin/org/dataland/datalandbackend/
│   ├── api/
│   ├── controller/
│   ├── entities/
│   ├── repositories/
│   └── services/
└── src/test/kotlin/

dataland-qa-service/
├── src/main/kotlin/org/dataland/datalandqaservice/
│   ├── api/
│   ├── controller/
│   ├── entities/
│   ├── repositories/
│   └── services/
└── src/test/kotlin/

dataland-data-sourcing-service/
├── src/main/kotlin/org/dataland/datasourcingservice/
│   ├── api/
│   ├── model/
│   ├── repositories/
│   ├── services/
│   └── utils/
└── src/test/kotlin/

dataland-message-queue-utils/
└── src/main/kotlin/org/dataland/datalandmessagequeueutils/

specs/004-unify-non-sourceability/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/
```

**Structure Decision**: Use the existing multi-service monorepo layout and
implement only bounded changes in backend metadata/sourceability modules, QA
review modules, and data-sourcing state/security modules, plus feature-local
documentation contracts.

## Complexity Tracking

No constitution exceptions or complexity justifications are required for this feature.

## Phase Plan

### Phase 0 Research Output

Research confirms no unresolved clarifications remain. Key decisions cover:

1. Canonical uniqueness key is `(companyId, dataType, reportingPeriod)`.
2. `bypassQa=true` emits dedicated `non-sourceability-auto-accepted` event.
3. Delivery semantics are at-least-once with idempotent consumers.
4. Rejection path keeps data-sourcing state at `NonSourceableVerification` for
   manual QA handling.
5. End-to-end propagation SLO is 60 seconds.

### Phase 1 Design Scope

1. Backend:
   - Rewire active non-sourceability endpoint and processing usage to
     `NonSourceabilityInformationEntity` and update repository/search model,
     while retaining `SourceabilityEntity` as backup-only persistence.
   - Rewire `GET/POST/HEAD /metadata/nonSourceable` behavior to new entity
     semantics and `currentlyActive` checks.
   - Emit `non-sourceability-created` on standard POST and
     `non-sourceability-auto-accepted` when bypass is used.
   - Consume QA acceptance/rejection events and update `qaStatus` and
     `currentlyActive` accordingly.
2. QA service:
   - Introduce `NonSourceableQaReviewInformation` persistence model.
   - Add/rewire non-sourceable QA endpoints (`GET /nonSourceable`,
     `GET /nonSourceable/queue`, `POST /nonSourceable/{nonSourceabilityId}`).
   - Consume backend non-sourceability-created event and create QA review row.
   - Emit accepted/rejected QA decision events.
3. Data sourcing service:
   - Add `NonSourceableVerification` state and listener transitions from backend
     and QA events.
   - Update `SecurityUtilsService.canUserPatchState` role checks per state rules
     (admin-only for `NonSourceable`; document-collector permission for
     `DocumentSourcing -> DocumentSourcingDone`).

### Post-Design Constitution Re-Check

Status after Phase 1 design: PASS

- Contracts and messaging artifacts are explicitly versioned and consumer-aware.
- No constitution gate exception required.
