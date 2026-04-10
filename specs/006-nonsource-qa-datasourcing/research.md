# Research: NonSourceability QA Tests — Data Sourcing Integration & Rejected Path

**Branch**: `006-nonsource-qa-datasourcing` | **Date**: 2026-04-10

## Summary

All unknowns resolved from generated client source and existing test patterns.
Zero new dependencies. All required API clients already on `ApiAccessor`.

---

## Finding 1 — Data Sourcing Initialization Flow

**Decision**: Two-step initialization using `dataSourcingRequestControllerApi`.

**Step 1** — Create request (as `TechnicalUser.Admin`):
```kotlin
val requestId = asAdmin {
    apiAccessor.dataSourcingRequestControllerApi
        .createRequest(SingleRequest(companyId, dataType.value, reportingPeriod, "test"))
        .requestId
}
```
`createRequest` returns `SingleRequestResponse` (`requestId: String`).

**Step 2** — Patch state to Processing, obtain `dataSourcingId`:
```kotlin
val dataSourcingId = asAdmin {
    apiAccessor.dataSourcingRequestControllerApi
        .patchRequestState(requestId, RequestState.Processing)
        .dataSourcingEntityId!!
}
```
`patchRequestState` returns `StoredRequest`; `dataSourcingEntityId: String?` is the link to the DS object.
Non-null assert is safe here — a DS object is always created when the state transitions to `Processing`.

**Rationale**: All test calls use `asAdmin` (`TechnicalUser.Admin`) consistently, matching the pattern in `NonSourceabilityTest`.

---

## Finding 2 — Data Sourcing State Assertion

**Decision**: `getDataSourcingById(dataSourcingId).state` for single-object lookup by ID.

```kotlin
apiAccessor.dataSourcingControllerApi.getDataSourcingById(dataSourcingId).state
```

Returns `StoredDataSourcing` with `state: DataSourcingState`.

Alternatives considered:
- `searchDataSourcings(companyId, dataType, reportingPeriod).first().state` — also works but is slower; `getDataSourcingById` is preferred when the ID is known.

---

## Finding 3 — DataSourcingState Enum Values

Source: `dataland-e2etests/build/clients/data-sourcing-service/src/main/kotlin/.../DataSourcingState.kt`

```kotlin
enum class DataSourcingState {
    Initialized, DocumentSourcing, DocumentSourcingDone,
    DataExtraction, DataVerification,
    NonSourceableVerification,   // ← expected after POST non-sourceability
    NonSourceable,               // ← expected after QA Accepted
    Done
}
```

`NonSourceableVerification` and `NonSourceable` are the two states under test.

---

## Finding 4 — Imports Required

All data-sourcing-related types live under `org.dataland.dataSourcingService.openApiClient.model.*`:
```kotlin
import org.dataland.dataSourcingService.openApiClient.model.DataSourcingState
import org.dataland.dataSourcingService.openApiClient.model.RequestState
import org.dataland.dataSourcingService.openApiClient.model.SingleRequest
```

---

## Finding 5 — Ctx Extension Strategy

The existing `Ctx` data class in `NonSourceabilityTest`:
```kotlin
private data class Ctx(val companyId: String, val dataType: DataTypeEnum, val reportingPeriod: String)
```

**Decision**: Add `dataSourcingId: String? = null` with a default of `null`.
- The first `@Test` (bypassQa=true) does not use DS — default null keeps it unchanged.
- The two new tests set `dataSourcingId` explicitly after initialization.

---

## Finding 6 — DS State Transition Timing

DS state `NonSourceableVerification` is set asynchronously after the non-sourceability entry is posted (event-driven via RabbitMQ). `awaitUntilAsserted` (max 2s, 500ms poll delay) already used for all async hops in the test — consistent with FR-009 and SC-003.

`NonSourceable` after QA acceptance is also async (same pattern).

For the rejected path, the DS state does **not** change — assertion is synchronous (no polling needed).

---

## Finding 7 — Role Note

All API calls use `asAdmin` (`TechnicalUser.Admin`), consistent with the rest of `NonSourceabilityTest`.
