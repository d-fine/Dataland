# Implementation Plan: Remove Legacy Sourceability System

**Branch**: `009-remove-legacy-sourceability` | **Date**: 2026-04-14 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/009-remove-legacy-sourceability/spec.md`

## Summary

Remove the legacy append-only sourceability event log code paths from backend, community-manager, and message-queue-utils services. The new `NonSourceabilityInformationManager`-based lifecycle with QA review is the canonical system and remains untouched. This is a pure deletion/reduction task — no new code is written, no database tables are dropped, no OpenAPI contracts change, and no client regeneration is required.

## Technical Context

**Language/Version**: Kotlin on JVM 21  
**Primary Dependencies**: Spring Boot, Spring AMQP/RabbitMQ, JPA/Hibernate  
**Storage**: PostgreSQL (`data_sourceability` table preserved, not dropped)  
**Testing**: JUnit 5 (Kotlin), Cypress (frontend component tests)  
**Target Platform**: Docker-based multi-service platform  
**Project Type**: Multi-service web platform (removal task across 3 services + tests)  
**Performance Goals**: N/A (code removal only)  
**Constraints**: Must not break new-system message routing; shared exchange must be preserved  
**Scale/Scope**: 14 files affected (6 deletions, 6 modifications, 2 verifications)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| **I. Contract-First Service Boundaries** | **PASS** | No OpenAPI contract changes — legacy types are not exposed in `MetaDataApi`. No client regeneration needed. |
| **II. Backward-Compatible Messaging** | **PASS** | Shared exchange `BACKEND_DATA_NONSOURCEABLE` preserved. Only removing a dead routing key (`DATA_NONSOURCEABLE`) with no active producers or consumers after removal. `SourceabilityMessage` class preserved for data-sourcing → user-service path. |
| **III. Microservice Autonomy** | **PASS** | Changes are scoped per service. Each service remains independently deployable after removal. |
| **IV. Mandatory Test Coverage** | **PASS** | Tests for deleted code are deleted. Tests for modified code are adapted. New-system test coverage is 100% preserved. No coverage regression — removing dead code cannot reduce coverage baselines. |
| **V. Traceability & Validation** | **PASS** | No observable flows are removed — the legacy path had no active producers. New-system logging/tracing is untouched. |
| **VI. Minimal Dependencies & Changes** | **PASS** | Pure removal — smallest possible change. No new dependencies. No refactoring beyond legacy cleanup. |

**Gate result: ALL PASS — proceed to Phase 0.**

## Project Structure

### Documentation (this feature)

```text
specs/009-remove-legacy-sourceability/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output (removal inventory)
├── quickstart.md        # Phase 1 output (verification guide)
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root — files affected)

```text
dataland-backend/src/main/kotlin/org/dataland/datalandbackend/
├── controller/MetaDataController.kt                          # MODIFY: remove dormant dependency
├── services/SourceabilityDataManager.kt                      # DELETE
├── entities/SourceabilityEntity.kt                           # DELETE
├── repositories/SourceabilityDataRepository.kt               # DELETE
├── repositories/utils/NonSourceableDataSearchFilter.kt       # DELETE
├── model/metainformation/SourceabilityInfo.kt                # DELETE
└── model/metainformation/SourceabilityInfoResponse.kt        # DELETE

dataland-backend/src/test/kotlin/org/dataland/datalandbackend/
└── services/SourceabilityDataManagerTest.kt                  # DELETE

dataland-community-manager/src/main/kotlin/org/dataland/datalandcommunitymanager/
├── services/CommunityManagerListener.kt                      # MODIFY: remove legacy listener + helpers
└── services/DataRequestUpdateManager.kt                      # MODIFY: remove SourceabilityMessage overload

dataland-community-manager/src/test/kotlin/org/dataland/datalandcommunitymanager/
├── services/CommunityManagerListenerUnitTest.kt              # MODIFY: remove 3 legacy test methods
├── services/DataRequestUpdateManagerTest.kt                  # MODIFY: adapt 3 tests to 3-param overload
└── utils/DataRequestUpdateManagerTestDataProvider.kt         # MODIFY: remove 2 legacy helpers

dataland-message-queue-utils/src/main/kotlin/org/dataland/datalandmessagequeueutils/
├── constants/MessageType.kt                                  # MODIFY: remove DATA_NONSOURCEABLE entry
└── constants/RoutingKeyNames.kt                              # MODIFY: remove DATA_NONSOURCEABLE entry

dataland-frontend/tests/
├── component/components/pages/ViewDataRequestPageLegacy.cy.ts  # MODIFY: remove nonSourceable test case
└── e2e/specs/admin-tools/RabbitMQAdmin.ts                      # MODIFY: remove legacy queue name
```

**Structure Decision**: Existing multi-service monorepo structure. No new files or directories created. Changes are scoped to 3 backend services and 2 test files in the frontend.

## Complexity Tracking

> No constitution violations — this section is intentionally empty.
