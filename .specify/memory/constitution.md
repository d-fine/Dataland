<!--
Sync Impact Report
- Version change: 1.1.0 -> 1.2.0
- Modified principles:
  - III. Mandatory Test Coverage Across Kotlin and Vue (Non-Negotiable) -> III. Mandatory Test Coverage and Coverage Floor (Non-Negotiable)
  - IV. Traceability and Operational Clarity -> IV. Traceability, Validation, and Operational Clarity
  - V. Minimal, Safe, and Reviewable Changes -> V. Minimal Dependencies and Reviewable Changes
- Added sections:
  - Service and Platform Baseline
- Removed sections: None
- Templates requiring updates:
  - ✅ .specify/templates/plan-template.md
  - ✅ .specify/templates/spec-template.md
  - ✅ .specify/templates/tasks-template.md
  - ✅ .specify/templates/commands/*.md (not present; no update required)
  - ✅ README.md (no update required)
- Follow-up TODOs: None
-->

# Dataland Constitution

## Core Principles

### I. Contract-First Service Boundaries
All cross-service behavior must be defined through explicit contracts (OpenAPI,
message schemas, and shared model classes). Any contract change requires
identifying consumers, updating compatibility notes, and adapting affected tests
before merge. OpenAPI contracts must remain valid OpenAPI 3.x and be reviewed
for backward compatibility.

### II. Backward-Compatible Messaging by Default
Message producers and listeners must evolve safely. New fields are additive and
optional unless a coordinated breaking rollout is approved. Queue bindings,
message types, and routing keys must remain stable for existing consumers.

### III. Microservice Autonomy

Each service (backend, qa-service, document-manager, community-manager, etc.)
MUST be independently deployable and independently testable without requiring
the full stack to be running. Asynchronous communication via RabbitMQ MUST be
preferred wherever eventual consistency is acceptable; synchronous REST calls
between services MUST be limited to read-only queries with no side effects.

### IV. Mandatory Test Coverage and Coverage Floor (Non-Negotiable)
Every change MUST include tests at the level where behavior is introduced.
Kotlin services MUST include unit tests for domain logic and integration tests
for service boundaries, persistence, and messaging contracts. Vue frontend
changes MUST include component or unit tests for behavior and end-to-end tests
for critical user flows when UI journeys are affected. Bug fixes MUST include a
regression test that fails before the fix. Changed modules MUST keep line
coverage at or above 80% and MUST not reduce existing coverage baselines.

### V. Traceability, Validation, and Operational Clarity
Asynchronous flows must be observable. Message handlers and emitters must log
correlation identifiers and essential data dimensions. Fail-fast validation is
required for malformed or incomplete external input.

### VI. Minimal Dependencies and Reviewable Changes
Prefer the smallest change that solves the problem. Avoid broad refactors in
feature work unless required for correctness. Avoid adding new dependencies
unless an existing dependency cannot satisfy the requirement and rationale is
documented in the plan. Keep changes scoped, readable, and reversible, with
clear rationale in specs and pull request descriptions.

## Technology Standards

The following technology choices are authoritative for new work.
Deviations require a documented rationale and constitution amendment.

| Layer            | Technology                                |
|------------------|-------------------------------------------|
| Backend language | Kotlin on JVM 21                          |
| Backend framework| Spring Boot + Spring Security             |
| Frontend         | Vue 3 + TypeScript (strict mode)          |
| UI components    | PrimeVue 4                                |
| Data fetching    | TanStack Vue Query                        |
| Auth provider    | Keycloak                                  |
| Message queue    | RabbitMQ                                  |
| Database         | PostgreSQL                                |
| E2E tests        | Cypress                                   |
| Build system     | Gradle (JVM), npm (frontend)              |
| Linting          | ktlint (Kotlin), ESLint (TypeScript)      |
| Infrastructure   | Docker / Docker Compose                   |
| License          | GNU AGPL-3.0                              |



## Engineering Constraints

- Primary backend implementation remains Kotlin with Gradle-based builds.
- Primary frontend implementation remains Vue with Node-based tooling.
- Shared contracts and utility modules are preferred over duplicating schema
  logic across services.
- Code duplication MUST be actively reduced; new duplication requires explicit
  technical justification.
- Data handling and service behavior must respect repository licensing and
  contribution requirements documented in `README.md` and `contribution/`.
- Security-sensitive behavior (authentication, authorization, queue input
  validation, and data access) must be explicitly validated and tested.

## Delivery Workflow and Quality Gates

1. Create or update `specs/<feature>/spec.md` before implementation.
2. Produce `plan.md` with technical approach, dependencies, and risk notes.
3. Break work into testable tasks in `tasks.md`.
4. Implement incrementally with passing local tests for impacted modules.
5. Before review, verify affected unit/integration/e2e tests and confirm no
   untracked contract regressions.

## Governance

This constitution is the default policy for all feature specs under `.specify/`.
Amendments require:
1. A documented rationale and scope.
2. Version update in this file.
3. Communication of migration impact to active feature branches.

Versioning policy for this constitution follows semantic versioning:
1. MAJOR for backward-incompatible governance changes or principle
   removals/redefinitions.
2. MINOR for new principles/sections or materially expanded mandatory guidance.
3. PATCH for wording clarifications, typo fixes, and non-semantic refinements.

Reviews must explicitly check constitutional compliance for contract changes,
testing completeness, and operational safety.

**Version**: 1.2.0 | **Ratified**: 2026-03-30 | **Last Amended**: 2026-04-01