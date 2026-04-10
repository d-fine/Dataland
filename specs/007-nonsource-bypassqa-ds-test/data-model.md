# Data Model: bypassQa=true Test with Data Sourcing Integration

**Feature**: `007-nonsource-bypassqa-ds-test` | **Date**: 2026-04-10

---

## State Transition (bypassQa=true path)

```
[DS init]
    createRequest → requestId
    patchRequestState(Processing) → dataSourcingEntityId

[DS state: Initialized]
    ↓ async (backend event: non-sourceability created)
[DS state: NonSourceableVerification]   ← assertDsStateIsNonSourceableVerification(ctx)
    ↓ bypassQa=true POST response (synchronous acceptance)
      qaStatus=Accepted, currentlyActive=true           ← postNonSourceableWithBypassQa(ctx)
      QA service: no review row                         ← assertNoQaReviewRowExists(ctx)
      backend entry: Accepted + active                  ← assertBackendEntryIsAcceptedAndActive(ctx)
    ↓ async (backend event: non-sourceability accepted)
[DS state: NonSourceable]               ← assertDsStateIsNonSourceable(ctx)
```

---

## Test Method Structure

```kotlin
@Test
fun `POST nonSourceable with bypassQa true immediately accepts entry and transitions DS to NonSourceable`() {
    var ctx = Ctx(companyId = ..., dataType = sfdr, reportingPeriod = ...)
    ctx = ctx.copy(dataSourcingId = initializeDataSourcing(ctx.companyId))
    assertDsStateIsNonSourceableVerification(ctx)        // intermediate DS state check
    postNonSourceableWithBypassQa(ctx)                   // POST + assert response
    assertNoQaReviewRowExists(ctx)                       // QA service untouched
    assertBackendEntryIsAcceptedAndActive(ctx)           // backend immediately active
    assertDsStateIsNonSourceable(ctx)                    // DS terminal state
}
```

---

## New Helpers

| Helper | Return | Async? | Assertion |
|--------|--------|--------|-----------|
| `postNonSourceableWithBypassQa(ctx: Ctx)` | `Unit` | No | Response `qaStatus=Accepted`, `currentlyActive=true` |
| `assertNoQaReviewRowExists(ctx: Ctx)` | `Unit` | No | QA review list is empty |

### Reused Helpers (no changes)

| Helper | Source |
|--------|--------|
| `initializeDataSourcing(companyId: String): String` | Feature 006 |
| `assertDsStateIsNonSourceableVerification(ctx: Ctx)` | Feature 006 |
| `assertBackendEntryIsAcceptedAndActive(ctx: Ctx)` | Feature 005 |
| `assertDsStateIsNonSourceable(ctx: Ctx)` | Feature 006 |

---

## Ctx Usage

`Ctx` already has `dataSourcingId: String? = null` (added in feature 006). No changes needed.
