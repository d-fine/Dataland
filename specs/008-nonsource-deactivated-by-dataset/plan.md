# Implementation Plan: Non-Sourceability Deactivated by Dataset Upload

**Branch**: `008-nonsource-deactivated-by-dataset` | **Date**: 2026-04-13 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/008-nonsource-deactivated-by-dataset/spec.md`

## Summary

Add one new `@Test` to `NonSourceabilityTest.kt` that:
1. Creates an active non-sourceability entry (`bypassQa=true`).
2. Uploads a dataset for the same triple (`bypassQa=false`).
3. QA-approves the dataset via `qaServiceControllerApi.changeQaStatus`.
4. Asserts `currentlyActive = false` on the non-sourceability entry.

## Technical Context

**Language/Version**: Kotlin on JVM 21  
**Primary Dependencies**: JUnit 5, `awaitUntilAsserted`, auto-generated OpenAPI clients (`datalandbackend.openApiClient`, `datalandqaservice.openApiClient`)  
**Storage**: N/A (test-only; reads/writes via REST against a running stack)  
**Testing**: `./gradlew dataland-e2etests:test --tests "org.dataland.e2etests.tests.NonSourceabilityTest.*"`  
**Target Platform**: Running local Dataland stack (`manageLocalStack.sh --start --simple`)  
**Project Type**: Integration/E2E test  
**Performance Goals**: N/A  
**Constraints**: Async assertion must resolve within `awaitUntilAsserted` default window; no new flakiness allowed  
**Scale/Scope**: 1 new test method + 2 new private helpers in 1 existing file

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|---|---|---|
| I. Contract-First Service Boundaries | ✅ PASS | Consumes existing contracts; no new contracts |
| II. Backward-Compatible Messaging | ✅ PASS | No new messages |
| III. Microservice Autonomy | ✅ PASS | E2E test requires full stack — expected for E2E by definition |
| IV. Mandatory Test Coverage and Coverage Floor | ✅ PASS | This IS the test; adds coverage for the dataset-deactivates-nonSourceability path |
| V. Traceability, Validation, and Operational Clarity | ✅ PASS | Polling tolerates async propagation |
| VI. Minimal Dependencies and Reviewable Changes | ✅ PASS | Zero new dependencies; single file change |

**Post-design re-check**: All gates still pass. Single file change, no violations.

## Project Structure

### Documentation (this feature)

```text
specs/008-nonsource-deactivated-by-dataset/
├── plan.md       # This file
├── research.md   # Phase 0 output
├── data-model.md # Phase 1 output
└── quickstart.md # Phase 1 output
```

### Source Code (repository root)

```text
dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/
└── NonSourceabilityTest.kt   # 1 new @Test + 2 new private helpers added here
```

## Complexity Tracking

No constitution violations. No entry required.

## Phase Plan

### Phase 0: Research

No unknowns — all APIs to be called are exercised by existing tests in `NonSourceabilityTest.kt` and `DataSourcingServiceListenerTest.kt`. See [research.md](research.md).

### Phase 1: Design

See [data-model.md](data-model.md) and [quickstart.md](quickstart.md) for design details.

**Decision**: No new contracts. Existing `qaServiceControllerApi.changeQaStatus` approves a dataset; existing `metaDataControllerApi.getInfoOnNonSourceabilityOfDatasets` retrieves the non-sourceability state. The test follows the exact pattern of existing tests in the file.

**New helpers needed**:
- `uploadDatasetForTriple(ctx)` — uploads a dummy SFDR dataset for the ctx triple with `bypassQa=false`; returns the `dataId`.
- `assertNonSourceabilityIsInactive(ctx)` — polls `getInfoOnNonSourceabilityOfDatasets` until `currentlyActive = false`.
