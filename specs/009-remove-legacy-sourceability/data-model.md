# Data Model: Remove Legacy Sourceability System

**Feature**: 009-remove-legacy-sourceability  
**Date**: 2026-04-14

## Overview

This is a removal task — no new entities are introduced. This document serves as the **removal inventory**: a complete list of classes, methods, constants, and tests to delete or modify, organized by service.

## Entities Removed

### SourceabilityEntity (DELETE)
- **Table**: `data_sourceability` (table preserved — entity mapping removed)
- **Fields**: companyId, dataType, reportingPeriod, isNonSourceable, reason, userId, creationTime
- **Relationships**: ManyToOne mapped to company
- **File**: `dataland-backend/.../entities/SourceabilityEntity.kt`

### SourceabilityInfo (DELETE)
- **Purpose**: Request/message payload for legacy sourceability storage
- **Fields**: companyId, dataType, reportingPeriod, isNonSourceable, reason
- **File**: `dataland-backend/.../model/metainformation/SourceabilityInfo.kt`

### SourceabilityInfoResponse (DELETE)
- **Purpose**: API response model for legacy sourceability queries
- **Fields**: companyId, dataType, reportingPeriod, isNonSourceable, reason, userId, creationTime
- **File**: `dataland-backend/.../model/metainformation/SourceabilityInfoResponse.kt`

### NonSourceableDataSearchFilter (DELETE)
- **Purpose**: Filter DTO for legacy sourceability repository queries
- **Fields**: boolean predicates (shouldFilterByCompanyId, etc.)
- **File**: `dataland-backend/.../repositories/utils/NonSourceableDataSearchFilter.kt`

## Services Removed

### SourceabilityDataManager (DELETE)
- **Purpose**: Legacy service managing append-only sourceability event log
- **Methods**: processSourceabilityDataStorageRequest, storeNonSourceableData, storeSourceableData, getSourceabilityDataByFilters, +2 others
- **Dependencies consumed**: SourceabilityEntity, SourceabilityDataRepository, NonSourceableDataSearchFilter, MessageType.DATA_NONSOURCEABLE, RoutingKeyNames.DATA_NONSOURCEABLE
- **File**: `dataland-backend/.../services/SourceabilityDataManager.kt`

### SourceabilityDataRepository (DELETE)
- **Purpose**: JPA repository for legacy sourceability entity queries
- **Methods**: getLatestSourceabilityInfoForDataset, searchNonSourceableData
- **File**: `dataland-backend/.../repositories/SourceabilityDataRepository.kt`

## Methods Removed (from preserved classes)

### CommunityManagerListener
| Method | Type | Reason |
|--------|------|--------|
| `processMessageForDataReportedAsNonSourceable()` | public @RabbitListener | Legacy consumer on routing key `DATA_NONSOURCEABLE` |
| `checkThatReceivedDataIsComplete(SourceabilityMessage)` | private | Only called by removed listener |
| `checkThatDatasetWasSetToNonSourceable(SourceabilityMessage)` | private | Only called by removed listener |

### DataRequestUpdateManager
| Method | Type | Reason |
|--------|------|--------|
| `patchAllNonWithdrawnRequestsToStatusNonSourceable(SourceabilityMessage, String)` | public @Transactional | Legacy overload — only called by removed listener |

### MetaDataController
| Element | Type | Reason |
|---------|------|--------|
| `@Autowired val sourceabilityDataManager: SourceabilityDataManager` | field | Dormant dependency on deleted class |
| `import ...SourceabilityDataManager` | import | Import for deleted class |
| KDoc `@param sourceabilityDataManager ...` | comment | Documents deleted parameter |

## Constants Removed

### MessageType.kt
| Constant | Value | Reason |
|----------|-------|--------|
| `DATA_NONSOURCEABLE` | `"Data non-sourceable"` | Only used by deleted producer + consumer |

### RoutingKeyNames.kt
| Constant | Value | Reason |
|----------|-------|--------|
| `DATA_NONSOURCEABLE` | `"dataNonSourceable"` | Legacy routing key with no remaining consumers |

## Constants Preserved (explicit safety list)

| Constant | File | Reason preserved |
|----------|------|-----------------|
| `ExchangeName.BACKEND_DATA_NONSOURCEABLE` | ExchangeName.kt | Shared exchange — new system publishes to it |
| `ExchangeName.DATASOURCING_DATA_NONSOURCEABLE` | ExchangeName.kt | Used by data-sourcing → user-service path |
| `MessageType.DATASOURCING_NONSOURCEABLE` | MessageType.kt | Used by data-sourcing service |
| `MessageType.NON_SOURCEABILITY_CREATED` | MessageType.kt | Used by new system |
| `MessageType.NON_SOURCEABILITY_AUTO_ACCEPTED` | MessageType.kt | Used by new system |
| `MessageType.NON_SOURCEABILITY_QA_ACCEPTED` | MessageType.kt | Used by new system |
| `MessageType.NON_SOURCEABILITY_QA_REJECTED` | MessageType.kt | Used by new system |
| `RoutingKeyNames.DATASOURCING_NONSOURCEABLE` | RoutingKeyNames.kt | Used by data-sourcing service |
| `RoutingKeyNames.NON_SOURCEABILITY_*` (4 entries) | RoutingKeyNames.kt | Used by new system |
| `SourceabilityMessage` class | messages/ | Used by DataSourcingManager → user-service notification |
| `QueueNames.USER_SERVICE_NON_SOURCABLE_EVENT` | QueueNames.kt | User-service notification listener queue |

## Database State

| Table | Action | Reason |
|-------|--------|--------|
| `data_sourceability` | **NO CHANGE** | Preserved for future data migration (FR-009) |
| `non_sourceability_information` | **NO CHANGE** | Active table for new system |

## Comment Update

### NonSourceabilityInformationManager.kt
- Remove KDoc reference: "`[SourceabilityDataManager] is retained as backup-only.`"
- Replace with: "`Manages the canonical non-sourceability lifecycle in the backend.`" (remove the backup-only sentence entirely)
