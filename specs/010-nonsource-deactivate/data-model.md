# Data Model: Deactivate Non-Sourceability via Endpoint

**Feature**: `010-nonsource-deactivate`  
**Phase**: 1 — Design

---

## Entities

### NonSourceabilityInformationEntity (unchanged)

No schema changes. All required columns already exist.

| Column | Type | Notes |
|---|---|---|
| `non_sourceability_id` | UUID PK | Auto-generated |
| `company_id` | String | Part of triple key |
| `data_type` | String (converted) | Part of triple key |
| `reporting_period` | String | Part of triple key |
| `qa_status` | Enum (Pending/Accepted/Rejected) | Mutable |
| `uploader_user_id` | String | Set from auth context |
| `upload_time` | Long (epoch ms) | Set at upload time |
| `currently_active` | Boolean | Mutable — the key field |
| `reason` | String? | Optional free text |
| `bypass_qa` | Boolean | Whether QA was bypassed |

---

## Request Model Change

### NonSourceabilityRequest — add `currentlyActive` field

```kotlin
// NEW — add after existing `reason` field:
@field:JsonProperty(required = true)
@field:Schema(description = "When true the triple is treated as non-sourceable; when false the entry is inactive. " +
    "Must be false when bypassQa=false (the QA service sets this upon approval). " +
    "When bypassQa=true, this signals whether the intent is to mark non-sourceable (true) or sourceable (false).")
val currentlyActive: Boolean
```

---

## Service Logic (NonSourceabilityInformationManager)

### `processNonSourceabilityRequest` — refactored branching

The single method is split into four logical paths based on `(bypassQa, currentlyActive)`:

```
┌──────────────────────────────────────────────────────────────────────────────┐
│  bypassQa=false, currentlyActive=true                                        │
│  → InvalidInputApiException (400)  — invalid combination                     │
├──────────────────────────────────────────────────────────────────────────────┤
│  bypassQa=false, currentlyActive=false   (standard QA path)                  │
│  1. existsActiveOrPendingForTuple([Pending]) → ConflictApiException (409)    │
│  2. findActiveForTuple().isNotEmpty()    → ConflictApiException (409)        │
│  3. Create entity: qaStatus=Pending, currentlyActive=false                   │
│  4. Emit NON_SOURCEABILITY_CREATED event                                     │
├──────────────────────────────────────────────────────────────────────────────┤
│  bypassQa=true, currentlyActive=true   (admin bypass — mark non-sourceable)  │
│  1. existsActiveOrPendingForTuple([Pending]) → ConflictApiException (409)    │
│  2. findActiveForTuple().isNotEmpty()    → ConflictApiException (409)        │
│  3. Create entity: qaStatus=Accepted, currentlyActive=true                   │
│  4. Emit NON_SOURCEABILITY_AUTO_ACCEPTED event                               │
├──────────────────────────────────────────────────────────────────────────────┤
│  bypassQa=true, currentlyActive=false  (admin reversal — mark sourceable)    │
│  1. existsActiveOrPendingForTuple([Pending]) → ConflictApiException (409)    │
│  2. findActiveForTuple() → if empty   → ConflictApiException (409, already   │
│       sourceable)                                                             │
│  3. Set existing active entry: currentlyActive=false  (save)                 │
│  4. Create new entity: qaStatus=Accepted, currentlyActive=false, bypassQa=T  │
│  5. NO event emitted                                                          │
│  6. Return new entity as response                                             │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## Repository (NonSourceabilityDataRepository) — no changes

Existing methods used by the new logic:

| Method | Used in new logic for |
|---|---|
| `findActiveForTuple(companyId, dataType, reportingPeriod)` | Check active exists; get entity to deactivate |
| `existsActiveOrPendingForTuple(…, listOf(QaStatus.Pending))` | Check pending exists |

---

## State Transition Diagram

```
Triple state:
  [Sourceable]
      │
      │ bypassQa=false / currentlyActive=false  (any uploader)
      ▼
  [Pending]  ──(QA reject)──► [Sourceable]
      │
      │ QA approves (existing QA service, out of scope)
      ▼
  [Non-sourceable]  (currentlyActive=true, qaStatus=Accepted)
      │
      │ bypassQa=true / currentlyActive=false  (admin reversal) [NEW]
      ▼
  [Sourceable]  +  audit entry (currentlyActive=false, qaStatus=Accepted)

  OR (shortcut):
  [Sourceable]
      │
      │ bypassQa=true / currentlyActive=true  (admin bypass)
      ▼
  [Non-sourceable]
```
