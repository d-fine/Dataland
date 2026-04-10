# Data Model: NonSourceability QA Tests — Data Sourcing Integration & Rejected Path

**Branch**: `006-nonsource-qa-datasourcing` | **Date**: 2026-04-10

## Overview

No new storage entities, database migrations, or API contract changes.
This feature is test-only. All entities below are already defined in generated OpenAPI clients.

---

## Existing Entity Used: `Ctx` (test-internal)

**Location**: `NonSourceabilityTest.kt` (private data class)

**Current**:
```kotlin
private data class Ctx(
    val companyId: String,
    val dataType: DataTypeEnum,
    val reportingPeriod: String,
)
```

**Changed to**:
```kotlin
private data class Ctx(
    val companyId: String,
    val dataType: DataTypeEnum,
    val reportingPeriod: String,
    val dataSourcingId: String? = null,
)
```

**Validation rules**: None — `dataSourcingId` is null for tests that don't use DS assertions.

---

## Existing API Models Consumed (read-only)

| Model | Package | Usage |
|---|---|---|
| `SingleRequest` | `dataSourcingService.openApiClient.model` | Request body for `createRequest` |
| `RequestState` | `dataSourcingService.openApiClient.model` | Enum value `Processing` for `patchRequestState` |
| `DataSourcingState` | `dataSourcingService.openApiClient.model` | Enum values `NonSourceableVerification`, `NonSourceable` for assertions |
| `StoredDataSourcing` | `dataSourcingService.openApiClient.model` | Return type of `getDataSourcingById`; `.state` field used |
| `StoredRequest` | `dataSourcingService.openApiClient.model` | Return type of `patchRequestState`; `.dataSourcingEntityId` field used |

---

## State Transition Model

```
[DS initialized]
    |
PATCH state → Processing
    |
    v
DataSourcingState.Initialized
    |
POST nonSourceabilityOfADataset  (async event → DS service)
    |
    v
DataSourcingState.NonSourceableVerification   ← assert here (with polling)
    |
POST QA decision
    |
    ├── Accepted → DataSourcingState.NonSourceable   ← assert here (with polling)
    └── Rejected → (unchanged: NonSourceableVerification)   ← assert here (no polling)
```
