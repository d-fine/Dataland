# Quickstart: Remove Legacy Sourceability System

**Feature**: 009-remove-legacy-sourceability  
**Date**: 2026-04-14

## Prerequisites

- JDK 21 installed
- Node.js 24 installed
- Gradle wrapper available (`./gradlew`)
- No running backend required for compilation checks

## Verification Steps

### 1. Verify backend compiles after legacy code removal

```bash
./gradlew dataland-backend:compileKotlin
```

Expected: BUILD SUCCESSFUL. Zero references to SourceabilityDataManager, SourceabilityEntity, SourceabilityDataRepository, NonSourceableDataSearchFilter, SourceabilityInfo, SourceabilityInfoResponse.

### 2. Verify community-manager compiles

```bash
./gradlew dataland-community-manager:compileKotlin
```

Expected: BUILD SUCCESSFUL. The legacy listener, helper methods, and SourceabilityMessage overload are gone. New-system listeners compile normally.

### 3. Verify message-queue-utils compiles

```bash
./gradlew dataland-message-queue-utils:compileKotlin
```

Expected: BUILD SUCCESSFUL. MessageType.DATA_NONSOURCEABLE and RoutingKeyNames.DATA_NONSOURCEABLE removed. All remaining constants intact.

### 4. Run backend unit tests

```bash
./gradlew dataland-backend:test
```

Expected: All tests pass. SourceabilityDataManagerTest.kt deleted — not in test suite.

### 5. Run community-manager unit tests

```bash
./gradlew dataland-community-manager:test
```

Expected: All tests pass. Legacy listener tests removed. Legacy overload tests adapted. New-system listener tests pass unchanged.

### 6. Verify no dangling references (codebase-wide)

```bash
# From repository root — search for references to deleted classes
grep -r "SourceabilityDataManager\|SourceabilityEntity\|SourceabilityDataRepository\|NonSourceableDataSearchFilter\|SourceabilityInfo\b" --include="*.kt" --include="*.ts" . | grep -v "build/" | grep -v "node_modules/" | grep -v ".gradle/"
```

Expected: Zero matches in source code (frontend auto-generated clients in `build/` are excluded and will be regenerated on next build).

### 7. Verify preserved components still function

```bash
# Compile all affected services together
./gradlew dataland-backend:compileKotlin dataland-community-manager:compileKotlin dataland-message-queue-utils:compileKotlin dataland-user-service:compileKotlin dataland-data-sourcing-service:compileKotlin
```

Expected: All services compile. No cross-service breakage.

### 8. Full test suite (CI-equivalent)

```bash
./gradlew dataland-backend:test dataland-community-manager:test
```

Expected: All tests pass. No new failures introduced.

## What NOT to verify (out of scope)

- Frontend production behavior — legacy pages may break, this is accepted
- E2E tests requiring full stack — these test the new system which is unchanged
- Database migrations — no schema changes made
- RabbitMQ broker queue state — physical queues are a broker admin concern
