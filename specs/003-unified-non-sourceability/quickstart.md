# Quickstart Guide: Unified Non-Sourceability Lifecycle

**Date**: 2026-04-07  
**Feature**: Unified Non-Sourceability Lifecycle  
**Branch**: `003-unified-non-sourceability`

This guide helps developers set up, test, and understand the non-sourceability feature across three Dataland microservices.

---

## Prerequisites

- Java 17+ (for Kotlin/Spring Boot services)
- Docker & Docker Compose
- Git
- IDE: IntelliJ IDEA (recommended) or VS Code with Kotlin extension
- Postman or curl for API testing

---

## Local Environment Setup

### 1. Start Infrastructure (PostgreSQL + RabbitMQ)

From the repository root, update the `docker-compose.yml` to include PostgreSQL and RabbitMQ if not already present:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: dataland
      POSTGRES_USER: dataland
      POSTGRES_PASSWORD: dataland
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  rabbitmq:
    image: rabbitmq:3.12-management
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    ports:
      - "5672:5672"    # AMQP port
      - "15672:15672"  # Management UI
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq

volumes:
  postgres_data:
  rabbitmq_data:
```

Start services:

```bash
docker-compose up -d postgres rabbitmq
```

Verify RabbitMQ is running:
- Management UI: http://localhost:15672 (guest/guest)

### 2. Configure RabbitMQ Topology

Create the exchanges, queues, and bindings. Run this script in a terminal:

```bash
# Connect to RabbitMQ and declare topology
docker exec rabbitmq rabbitmqctl declare_exchange dataland-events topic durable=true
docker exec rabbitmq rabbitmqctl declare_queue non-sourceability-created-queue durable=true
docker exec rabbitmq rabbitmqctl declare_queue non-sourceability-auto-accepted-queue durable=true
docker exec rabbitmq rabbitmqctl declare_queue qa-decision-queue durable=true
docker exec rabbitmq rabbitmqctl declare_queue dataland-events-dlq durable=true

# Bind queues to exchange
docker exec rabbitmq rabbitmqctl bind_queue non-sourceability-created-queue dataland-events "non-sourceability.created"
docker exec rabbitmq rabbitmqctl bind_queue non-sourceability-auto-accepted-queue dataland-events "non-sourceability.auto-accepted"
docker exec rabbitmq rabbitmqctl bind_queue qa-decision-queue dataland-events "qa.decision.*"
```

Verify in RabbitMQ UI:
- Exchanges: `dataland-events` (type: topic)
- Queues: 4 queues listed above

---

## Running the Microservices Locally

Each service runs independently on a different port.

### Backend Service (dataland-backend)

```bash
cd dataland-backend

# Build
./gradlew build

# Run on port 8080
./gradlew bootRun --args='--server.port=8080'
```

**Verify**:
```bash
curl -X GET http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

### QA Service (dataland-qa-service)

```bash
cd dataland-qa-service

# Build
./gradlew build

# Run on port 8081
./gradlew bootRun --args='--server.port=8081'
```

**Verify**:
```bash
curl -X GET http://localhost:8081/actuator/health
```

### Data-Sourcing Service (dataland-data-sourcing-service)

```bash
cd dataland-data-sourcing-service

# Build
./gradlew build

# Run on port 8082
./gradlew bootRun --args='--server.port=8082'
```

**Verify**:
```bash
curl -X GET http://localhost:8082/actuator/health
```

---

## Testing the Feature End-to-End

### Scenario 1: Create Request with QA Review (bypassQa=false)

```bash
# 1. Create non-sourceability request
curl -X POST http://localhost:8080/metadata/nonSourceable \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "companyId": "550e8400-e29b-41d4-a716-446655440000",
    "dataType": "sustainability-data",
    "reportingPeriod": "2024-12-31",
    "reason": "Data quality issues identified",
    "bypassQa": false
  }'

# Expected 201:
# {
#   "nonSourceabilityId": "123e4567-e89b-12d3-a456-426614174000",
#   "companyId": "550e8400-e29b-41d4-a716-446655440000",
#   "dataType": "sustainability-data",
#   "reportingPeriod": "2024-12-31",
#   "reason": "Data quality issues identified",
#   "uploaderUserId": "user-1",
#   "uploadTime": "2026-04-07T10:30:00Z",
#   "qaStatus": "PENDING",
#   "currentlyActive": false
# }

# 2. Check that QA review task was created
curl -X GET 'http://localhost:8081/nonSourceable/queue' \
  -H "Authorization: Bearer <TOKEN>"

# Expected 200: Should see pending review with nonSourceabilityId from step 1

# 3. QA reviewer submits acceptance
curl -X POST http://localhost:8081/nonSourceable/123e4567-e89b-12d3-a456-426614174000 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <QA_REVIEWER_TOKEN>" \
  -d '{
    "qaStatus": "ACCEPTED",
    "qaComment": "Validated data quality issues"
  }'

# Expected 200: Review record updated with reviewer ID and comment

# 4. Verify backend record was updated
curl -X GET 'http://localhost:8080/metadata/nonSourceable?nonSourceabilityId=123e4567-e89b-12d3-a456-426614174000' \
  -H "Authorization: Bearer <TOKEN>"

# Expected: qaStatus=ACCEPTED, currentlyActive=true

# 5. Verify data-sourcing state was updated
curl -X GET 'http://localhost:8082/data-sourcing/<dataSourcingId>/state' \
  -H "Authorization: Bearer <TOKEN>"

# Expected: state=NonSourceable
```

### Scenario 2: QA Acceptance Detailed Flow

```bash
# 1. Create request (same as above, bypassQa=false)
curl -X POST http://localhost:8080/metadata/nonSourceable \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "companyId": "550e8400-e29b-41d4-a716-446655440000",
    "dataType": "sfdr",
    "reportingPeriod": "2023",
    "reason": "Data quality issues identified",
    "bypassQa": false
  }'

# Response contains: nonSourceabilityId (e.g., "123e4567-e89b-12d3-a456-426614174000")

# 2. Backend publishes NON_SOURCEABILITY_CREATED event via RabbitMQ
# 3. QA Service receives event and creates a pending review task automatically

# 4. QA reviewer retrieves pending reviews
curl -X GET 'http://localhost:8081/nonSourceable' \
  -H "Authorization: Bearer <QA_REVIEWER_TOKEN>"

# Expected 200: List of pending NonSourceableQaReviewInformationEntity objects

# 5. QA reviewer submits acceptance decision
curl -X POST 'http://localhost:8081/nonSourceable/123e4567-e89b-12d3-a456-426614174000' \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <QA_REVIEWER_TOKEN>" \
  -d '{
    "qaStatus": "Accepted",
    "qaComment": "Validated. Data quality issues confirmed."
  }'

# Expected 200: Review accepted, event QA_NON_SOURCEABILITY_ACCEPTED published

# 6. Backend receives QA_NON_SOURCEABILITY_ACCEPTED event and updates:
#    qaStatus=Accepted, currentlyActive=true
curl -X GET 'http://localhost:8080/metadata/nonSourceable/123e4567-e89b-12d3-a456-426614174000' \
  -H "Authorization: Bearer <TOKEN>"

# Expected: qaStatus=Accepted, currentlyActive=true

# 7. Data-Sourcing receives event and transitions to NonSourceable
curl -X GET 'http://localhost:8082/data-sourcing/<dataSourcingId>' \
  -H "Authorization: Bearer <TOKEN>"

# Expected: state=NonSourceable, associated requests marked as Processed
```

### Scenario 3: Create Request with QA Bypass (bypassQa=true – Admin only)

```bash
# 1. Create with bypass (admin user)
curl -X POST http://localhost:8080/metadata/nonSourceable \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -d '{
    "companyId": "550e8400-e29b-41d4-a716-446655440001",
    "dataType": "sustainability-data",
    "reportingPeriod": "2025-01-15",
    "reason": "Emergency: data sourcing must stop immediately",
    "bypassQa": true
  }'

# Expected 201:
# qaStatus=Accepted, currentlyActive=true (immediately, no QA needed)

# 2. Verify NO QA review task was created
curl -X GET 'http://localhost:8081/nonSourceable?nonSourceabilityId=<ID>' \
  -H "Authorization: Bearer <TOKEN>"

# Expected: Empty list or 404 (no review created for this request)

# 3. Verify data-sourcing auto-accepts and transitions to NonSourceable
curl -X GET 'http://localhost:8082/data-sourcing/<dataSourcingId>' \
  -H "Authorization: Bearer <TOKEN>"

# Expected: state=NonSourceable (direct transition via auto-accept event)
```

### Scenario 4: QA Rejection Detailed Flow

QA rejection is used when the non-sourceability claim is NOT valid. The request does **not** activate, and the dataset remains in `NonSourceableVerification` state pending manual follow-up.

```bash
# 1. Create request (with QA, bypassQa=false)
curl -X POST http://localhost:8080/metadata/nonSourceable \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "companyId": "550e8400-e29b-41d4-a716-446655440002",
    "dataType": "sustainability-data",
    "reportingPeriod": "2025-02-28",
    "reason": "Potential data quality issue",
    "bypassQa": false
  }'

# Response contains: nonSourceabilityId

# 2. Backend publishes NON_SOURCEABILITY_CREATED event
# 3. QA Service receives event and creates a pending review task

# 4. QA reviewer submits rejection decision
curl -X POST 'http://localhost:8081/nonSourceable/550e8400-e29b-41d4-a716-426614174002' \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <QA_REVIEWER_TOKEN>" \
  -d '{
    "qaStatus": "Rejected",
    "qaComment": "Data quality verified. No issues found. Request invalid."
  }'

# Expected 200: Review rejected, event QA_NON_SOURCEABILITY_REJECTED published

# 5. Backend receives QA_NON_SOURCEABILITY_REJECTED event and updates:
#    qaStatus=Rejected, currentlyActive=false (remains inactive)
curl -X GET 'http://localhost:8080/metadata/nonSourceable/550e8400-e29b-41d4-a716-426614174002' \
  -H "Authorization: Bearer <TOKEN>"

# Expected: qaStatus=Rejected, currentlyActive=false

# 6. Data-Sourcing remains in NonSourceableVerification
#    Associated requests are NOT marked as Processed
#    Manual QA team follow-up required
curl -X GET 'http://localhost:8082/data-sourcing/<dataSourcingId>' \
  -H "Authorization: Bearer <TOKEN>"

# Expected: state=NonSourceableVerification (unchanged)
#           Associated requests still in Open/Processing state
```

### Scenario 5: Idempotent Message Replay

Dataland's event system ensures **at-most-once delivery semantics**. If a QA decision event is replayed (due to message queue retry or manual recovery), the state remains unchanged.

```bash
# Simulate replaying the acceptance event multiple times
# Message ID and nonSourceabilityId are identical
# Expected: Only first delivery processes; subsequent replays are ignored

# First delivery:
#   QA_NON_SOURCEABILITY_ACCEPTED for nonSourceabilityId=XYZ
#   Backend: qaStatus → Accepted, currentlyActive → true

# Replay (same event):
#   QA_NON_SOURCEABILITY_ACCEPTED for nonSourceabilityId=XYZ
#   Backend: Checks if already processed, skips update;
#            state remains Accepted, currentlyActive=true

# Data-Sourcing behavior identical to backend:
#   First delivery: state → NonSourceable
#   Replay: Already in NonSourceable, idempotently keeps state
```

---

## Key Code Locations

### Backend Service (dataland-backend)

- **Controller**: `MetaDataController.kt`
  - POST /metadata/nonSourceable → Create non-sourceability request
  - GET /metadata/nonSourceable → Retrieve non-sourceability info
  - HEAD /metadata/nonSourceable/{companyId}/{dataType}/{reportingPeriod} → Check existence

- **Entity**: `NonSourceabilityInformationEntity.kt`
  - Fields: nonSourceabilityId (PK), companyId, dataType, reportingPeriod, qaStatus, currentlyActive
  - Table: `non_sourceability_information`

- **Service**: `SourceabilityDataManager.kt`
  - Business logic: duplicate detection, state validation, event publishing
  - Methods: `createNonSourceabilityRequest()`, `processQaNonSourceabilityAcceptedEvent()`, `processQaNonSourceabilityRejectedEvent()`

- **Event Listener**: `SourceabilityQaEventListener.kt`
  - Listens on exchange: `QA_SERVICE_DATA_QUALITY_EVENTS`
  - Routes: `qa.decision.accepted` → acceptance handler, `qa.decision.rejected` → rejection handler

### QA Service (dataland-qa-service)

- **Controller**: `QaController.kt`
  - GET /nonSourceable → List non-sourceability reviews
  - POST /nonSourceable/{nonSourceabilityId} → Submit decision (acceptance or rejection)

- **Entity**: `NonSourceableQaReviewInformationEntity.kt`
  - Fields: id (PK), nonSourceabilityId (FK), qaStatus, reviewerUserId, qaComment, createdAt, updatedAt
  - Indexes on nonSourceabilityId for fast lookup
  - Table: `non_sourceable_qa_review_information`

- **Service**: `QaReviewManager.kt`
  - Manages decision persistence and event emission
  - Methods: `handleNonSourceabilityDecision()` with bifurcated acceptance/rejection paths
  - Event methods: `sendNonSourceabilityAcceptedEvent()`, `sendNonSourceabilityRejectedEvent()`

- **Event Listener**: `QaEventListenerQaService.kt`
  - Listens on exchange: `dataland-backend.data-quality` (or topic queue)
  - Routes: `non-sourceability.created` → creates pending review with `qaStatus=Pending`

### Data-Sourcing Service (dataland-data-sourcing-service)

- **Event Listener**: `DataSourcingServiceListener.kt`
  - **Acceptance Path**: Listens on exchange: `QA_SERVICE_DATA_QUALITY_EVENTS`
    - Route: `qa.decision.accepted` → lookup dataSourcingId via correlationMap → patch state to `NonSourceable`
  - **Rejection Path**: Listens on same exchange
    - Route: `qa.decision.rejected` → lookup dataSourcingId → log rejection, keep state in `NonSourceableVerification`
  - Maintains in-memory map: `nonSourceabilityId → dataSourcingId` (populated during created event processing)

- **State Service**: `DataSourcingManager.kt`
  - Method: `patchDataSourcingState()` implements state transitions
  - Enforces: Only admins can directly patch to `NonSourceable` without QA approval
  - State machine: `Initialized → DocumentSourcing → NonSourceableVerification → NonSourceable | NonSourceableVerification (stays) | Done`

---

## Running Tests

### Unit Tests

```bash
# Backend
cd dataland-backend
./gradlew test --tests "*NonSourceable*"

# QA Service
cd dataland-qa-service
./gradlew test --tests "*NonSourceable*"

# Data-Sourcing Service
cd dataland-data-sourcing-service
./gradlew test --tests "*Sourcing*" --tests "*NonSourceable*"
```

### Integration Tests

```bash
# Requires Docker & Testcontainers
cd dataland-backend
./gradlew integrationTest --tests "*NonSourceable*Integration*"
```

### End-to-End Tests

See `scenarios` section above for manual E2E test flows.

---

## SLOs & Monitoring

### Success Criteria

- **SC-001**: 99% of requests reflected across services within 60 seconds
- **SC-006**: QA review task visible within 30 seconds (P99 latency)

### Monitoring & Observability

Check logs for event processing:

```bash
# Backend: Check for event publishing
grep "Published non-sourceability-created" dataland-backend/logs/*.log

# QA Service: Check for event consumption
grep "Received non-sourceability-created event" dataland-qa-service/logs/*.log

# Data-Sourcing: Check for state transitions
grep "State transitioned to NonSourceable" dataland-data-sourcing-service/logs/*.log
```

RabbitMQ monitoring:
- Open http://localhost:15672 (guest/guest)
- Check queues for depth (should drain to 0 if processing healthy)
- Check exchanges for publish rates

---

## Debugging

### Event Not Being Processed

1. **Check RabbitMQ queue depth**:
   ```bash
   curl -u guest:guest http://localhost:15672/api/queues
   ```
   If queue depth is growing, consumer may be down or erroring.

2. **Check service logs for errors**:
   ```bash
   grep ERROR dataland-qa-service/logs/*.log | grep -i nonsourceable
   ```

3. **Replay an event manually** (if message got to DLQ):
   - Use RabbitMQ Management UI to republish from dead-letter queue

### Duplicate Record Issue

- Check service logs for "Duplicate event received" warnings
- Confirm idempotency handler is working correctly
- Look at timestamps: are events arriving out of order?

### Authorization Denied on bypassQa

- Ensure JWT token has admin role in Keycloak
- Check `SOR-001` requirement: only admins can use bypassQa=true

---

## Next Steps

1. **Read the full spec**: [spec.md](spec.md)
2. **Review data model**: [data-model.md](../data-model.md)
3. **Check API contracts**: [contracts/](../contracts/)
4. **Study event flow**: [contracts/EVENTS.md](../contracts/EVENTS.md)
5. **Run tests**: Follow "Running Tests" section above
6. **Generate tasks**: Run `/speckit.tasks` to see implementation roadmap
