# Feature Specification: Non-Sourceability Deactivated by Dataset Upload

**Feature Branch**: `008-nonsource-deactivated-by-dataset`  
**Created**: 2026-04-13  
**Status**: Draft  
**Input**: User description: "Add a test verifying that a currentlyActive nonSourceability entry's currentlyActive flag becomes false after a dataset for the same triple is uploaded and QA-approved"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Non-Sourceability Deactivated by QA-Approved Dataset (Priority: P1)

A data reviewer wants to confirm that uploading and QA-approving a real dataset for a company/dataType/reportingPeriod triple supersedes an existing non-sourceability declaration. Once QA accepts the dataset, the system must mark the non-sourceability entry as no longer active so that users know real data is now available.

**Why this priority**: This is the single behaviour being validated; it is the entire feature scope.

**Independent Test**: Can be fully tested by setting up a currently-active non-sourceability entry (via `bypassQa=true`), uploading a dataset for the same triple, QA-approving that dataset, and asserting that `currentlyActive` on the non-sourceability entry has changed to `false`.

**Acceptance Scenarios**:

1. **Given** a non-sourceability entry exists for a triple (companyId, dataType, reportingPeriod) with `currentlyActive = true` and `qaStatus = Accepted`, **When** a dataset is uploaded for that same triple with `bypassQa = false`, **Then** the system awaits QA review and the non-sourceability entry's `currentlyActive` remains `true` until QA acts.
2. **Given** a dataset is pending QA for the triple, **When** QA approves the dataset, **Then** the system sets `currentlyActive = false` on the non-sourceability entry for that triple.

---

### Edge Cases

- The non-sourceability entry must already be `Accepted` and `currentlyActive = true` before the dataset upload; a `Pending` or `Rejected` entry is a different scenario already covered by existing tests.
- Dataset uploaded for a *different* triple must not affect the non-sourceability entry for the original triple.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST set `currentlyActive = false` on a non-sourceability entry when a QA-accepted dataset exists for the same (companyId, dataType, reportingPeriod) triple.
- **FR-002**: The system MUST keep `currentlyActive = true` on the non-sourceability entry until QA explicitly accepts the dataset.
- **FR-003**: The transition from `currentlyActive = true` to `false` MUST be reflected in the GET non-sourceability endpoint within a reasonable propagation window after QA acceptance is recorded.

### Key Entities

- **NonSourceabilityEntry**: Tracks whether data for a triple is known to be unsourceable; has `currentlyActive` flag, `qaStatus`, and `nonSourceabilityId`.
- **Dataset**: An uploaded data record for a (companyId, dataType, reportingPeriod) triple; has `dataId` and `qaStatus`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: After QA approves a dataset for a triple, the corresponding non-sourceability entry's `currentlyActive` flag transitions to `false` within the standard event-propagation window (consistent with existing async assertions in the test suite).
- **SC-002**: The state change is visible via the GET non-sourceability endpoint without any manual intervention.
- **SC-003**: The test passes reliably across repeated runs against a clean local stack, with no flaky assertions.

## Assumptions

- The test targets the SFDR framework, consistent with all existing `NonSourceabilityTest` scenarios.
- The exact reporting period (`2026`) and data type (`sfdr`) match those used throughout the existing test class.
- QA approval for datasets uses the existing `qaServiceControllerApi.changeQaStatus` mechanism already exercised in other tests.
- The local Dataland stack is running when the test is executed.
