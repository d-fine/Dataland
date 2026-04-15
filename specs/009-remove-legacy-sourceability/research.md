# Research: Remove Legacy Sourceability System

**Feature**: 009-remove-legacy-sourceability  
**Date**: 2026-04-14

## Research Tasks & Findings

### R-1: Is `SourceabilityDataManager` actively called from `MetaDataController`?

- **Decision**: Safe to remove the dependency — it is completely dormant.
- **Rationale**: `MetaDataController` injects `sourceabilityDataManager` at line 47 but has **zero method calls** on it across the entire controller (~200 lines). The `NonSourceabilityInformationManager` comment (line 28) explicitly says "`SourceabilityDataManager` is retained as backup-only." The controller only uses `nonSourceabilityInformationManager` for the `/nonSourceable` endpoints.
- **Alternatives considered**: Leave the injection in place — rejected because it creates a compile dependency on a deleted class and increases confusion.

### R-2: Does removing legacy types affect the OpenAPI contract?

- **Decision**: No OpenAPI contract changes needed. No client regeneration required.
- **Rationale**: The `MetaDataApi` interface only references new-system types (`NonSourceabilityInformationResponse`, `NonSourceabilityRequest`). None of the legacy types (`SourceabilityInfo`, `SourceabilityInfoResponse`, `NonSourceableDataSearchFilter`) appear in any `@GetMapping`, `@PostMapping`, or `@RequestBody`/`@ResponseBody` annotations. The API surface is unchanged.
- **Alternatives considered**: Proactively regenerate clients — rejected because no definition changes exist to regenerate from.

### R-3: Does `SourceabilityMessage` reference `MessageType.DATA_NONSOURCEABLE`?

- **Decision**: `SourceabilityMessage` is structurally independent of the removed constant.
- **Rationale**: `SourceabilityMessage` is a plain data class with fields `basicDataDimensions`, `isNonSourceable`, `reason`. It does not import or reference `MessageType` at all. The `DATA_NONSOURCEABLE` constant is only used by the **producer** (`SourceabilityDataManager.processSourceabilityDataStorageRequest()` line 91) and **consumer** (`CommunityManagerListener.processMessageForDataReportedAsNonSourceable()` line 189) — both are being deleted. The remaining producer path (`DataSourcingManager.performStatePatch()`) uses `DATASOURCING_NONSOURCEABLE` routing key on the `DATASOURCING_DATA_NONSOURCEABLE` exchange, which is a completely separate path.
- **Alternatives considered**: Remove `SourceabilityMessage` too — rejected because it's still actively used by `DataSourcingManager` and the user-service notification listener.

### R-4: What is the legacy queue name situation?

- **Decision**: The queue name `community-manager.queue.nonSourceableData` is hardcoded only in the `@RabbitListener` annotation being deleted and in the `RabbitMQAdmin.ts` test — it is not in `QueueNames.kt`.
- **Rationale**: Searched `QueueNames.kt` (31 entries) — no entry matches `community-manager.queue.nonSourceableData`. The new-system queues have different names: `community-manager.processNonSourceabilityAutoAccepted` and `community-manager.processNonSourceabilityQaAccepted` (both in `QueueNames.kt`). The `RabbitMQAdmin.ts` test at line 23 contains the legacy queue name as an expected queue and must be updated.
- **Alternatives considered**: Also remove the physical queue from RabbitMQ — out of scope (broker admin task, not codebase change).

### R-5: Which `CommunityManagerListener` helper methods are legacy-only?

- **Decision**: Two private helper methods are legacy-only and can be removed: `checkThatReceivedDataIsComplete(SourceabilityMessage)` and `checkThatDatasetWasSetToNonSourceable(SourceabilityMessage)`.
- **Rationale**: Both accept `SourceabilityMessage` parameters and are called only from `processMessageForDataReportedAsNonSourceable()`. The new-system listeners use `validateLifecycleEvent(NonSourceabilityLifecycleEvent)` instead, which has different validation logic.
- **Alternatives considered**: Keep the helpers for potential future use — rejected because they operate on a type (`SourceabilityMessage`) that may be removed in a future task.

### R-6: What happens to tests calling the legacy overload in `DataRequestUpdateManagerTest`?

- **Decision**: Three tests need adaptation, one test should be deleted entirely.
- **Rationale**: 
  - Test `validate that providing information about a dataset that is sourceable throws` (line 376) calls `patchAllNonWithdrawnRequestsToStatusNonSourceable(dummySourceableInfo, correlationId)` where `dummySourceableInfo.isNonSourceable = false`. The 3-param overload has no `isNonSourceable` flag — this validation only existed in the legacy overload. **This test should be deleted** as the validation logic it tests is being removed.
  - Test `validate that notification behaviour is as expected` (line 386) calls the legacy overload with `dummyNonSourceableInfo`. Adapt to: `patchAllNonWithdrawnRequestsToStatusNonSourceable(companyId = dummyCompanyId, dataTypeAsString = "nuclear-and-gas", reportingPeriod = "dummyPeriod", correlationId = correlationId, requestStatusChangeReason = dummyRequestChangeReason)`.
  - Test `validate that patching corresponding requests` (line 413) — same adaptation pattern.
  - The `dummyNonSourceableInfo` and `dummySourceableInfo` properties (lines 74–75) and their provider methods in `DataRequestUpdateManagerTestDataProvider` should be removed.
- **Alternatives considered**: Keep the "sourceable throws" test with a different mechanism — rejected because the 3-param overload intentionally has no `isNonSourceable` guard; the new system's validation happens upstream.

### R-7: What is the `ViewDataRequestPageLegacy.cy.ts` nonSourceable test case?

- **Decision**: A single `it()` block should be deleted from the Cypress component test.
- **Rationale**: Line 210 contains `it('Check viewDataRequest page for nonSourceable request renders as expected and reopen data request', ...)`. This test creates a stored data request with `RequestStatus.NonSourceable` and verifies page rendering. The `RequestStatus.NonSourceable` enum value is NOT being removed (it's used by the new system), but this specific test exercises the legacy `ViewDataRequestPageLegacy` component which will stop working after backend changes. The test should be removed to prevent false failures.
- **Alternatives considered**: Keep the test since `RequestStatus.NonSourceable` still exists — rejected because the test exercises a legacy component page, and the spec explicitly says to delete this test case.

### R-8: Will the `NonSourceabilityInformationManager` comment need updating?

- **Decision**: Yes — the backup-only comment referencing `SourceabilityDataManager` should be cleaned up.
- **Rationale**: Line 28 of `NonSourceabilityInformationManager.kt` says "`[SourceabilityDataManager] is retained as backup-only.`" After deletion, this comment becomes a dangling reference. It should be removed or updated.
- **Alternatives considered**: Leave it as documentary history — rejected because it references a deleted class by name in a KDoc link, which would be a broken reference.

## Summary of All Resolved Unknowns

| # | Unknown | Resolution |
|---|---------|------------|
| R-1 | SourceabilityDataManager usage in controller | Zero method calls — dormant dependency, safe to remove |
| R-2 | OpenAPI contract impact | None — legacy types not exposed in API |
| R-3 | SourceabilityMessage ↔ MessageType.DATA_NONSOURCEABLE coupling | None — structurally independent |
| R-4 | Legacy queue name location | Hardcoded in annotation + RabbitMQAdmin.ts only, not in QueueNames.kt |
| R-5 | Legacy-only helper methods | `checkThatReceivedDataIsComplete` + `checkThatDatasetWasSetToNonSourceable` — both legacy-only |
| R-6 | Test adaptation strategy | Delete "sourceable throws" test; adapt 2 others to 3-param overload; remove 2 provider helpers |
| R-7 | ViewDataRequestPageLegacy test | Delete the nonSourceable `it()` block |
| R-8 | NonSourceabilityInformationManager comment | Update to remove dangling `SourceabilityDataManager` reference |
