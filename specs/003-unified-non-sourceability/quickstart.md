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

# Expected: state=NON_SOURCEABLE
```

### Scenario 2: Create Request with QA Bypass (bypassQa=true – Admin only)

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
# qaStatus=ACCEPTED, currentlyActive=true (immediately)

# 2. Verify NO QA review task was created
curl -X GET 'http://localhost:8081/nonSourceable?nonSourceabilityId=<ID>' \
  -H "Authorization: Bearer <TOKEN>"

# Expected: Empty list or 404 (no review created for this request)

# 3. Verify data-sourcing state is now NON_SOURCEABLE (not VERIFICATION)
curl -X GET 'http://localhost:8082/data-sourcing/<dataSourcingId>/state' \
  -H "Authorization: Bearer <TOKEN>"

# Expected: state=NON_SOURCEABLE (direct transition)
```

### Scenario 3: QA Rejection

```bash
# 1. Create request (with QA)
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

# 2. QA reviewer rejects
curl -X POST http://localhost:8081/nonSourceable/<NONSOURCEABILITY_ID> \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <QA_REVIEWER_TOKEN>" \
  -d '{
    "qaStatus": "REJECTED",
    "qaComment": "Data validated; no quality issues found"
  }'

# Expected 200

# 3. Verify backend status is REJECTED / currentlyActive=false
curl -X GET 'http://localhost:8080/metadata/nonSourceable?nonSourceabilityId=<ID>' \
  -H "Authorization: Bearer <TOKEN>"

# Expected: qaStatus=REJECTED, currentlyActive=false

# 4. Verify data-sourcing remains in NON_SOURCEABLE_VERIFICATION
curl -X GET 'http://localhost:8082/data-sourcing/<dataSourcingId>/state' \
  -H "Authorization: Bearer <TOKEN>"

# Expected: state=NON_SOURCEABLE_VERIFICATION (not transitioned)
```

---

## Key Code Locations

### Backend Service (dataland-backend)

- **Controller**: `dataland-backend/src/main/kotlin/com/d_fine/dataland/backend/metadata/nonsourceable/controller/NonSourceableMetadataController.kt`
  - POST /metadata/nonSourceable
  - GET /metadata/nonSourceable
  - HEAD /metadata/nonSourceable/{companyId}/{dataType}/{reportingPeriod}

- **Entity**: `dataland-backend/src/main/kotlin/com/d_fine/dataland/backend/metadata/nonsourceable/entity/NonSourceabilityInformationEntity.kt`

- **Service**: `dataland-backend/src/main/kotlin/com/d_fine/dataland/backend/metadata/nonsourceable/service/NonSourceableService.kt`
  - Business logic: duplicate detection, state validation, event publishing

- **Event Publisher**: `dataland-backend/src/main/kotlin/com/d_fine/dataland/backend/metadata/nonsourceable/service/NonSourceableEventPublisher.kt`
  - Publishes non-sourceability-created and non-sourceability-auto-accepted events

- **Event Listener**: `dataland-backend/src/main/kotlin/com/d_fine/dataland/backend/metadata/nonsourceable/service/QaDecisionEventListener.kt`
  - Listens for QA decisions (accepted/rejected), updates NonSourceabilityInformation

### QA Service (dataland-qa-service)

- **Controller**: `dataland-qa-service/src/main/kotlin/com/d_fine/dataland/qas/nonsourceable/controller/NonSourceableQaController.kt`
  - GET /nonSourceable
  - GET /nonSourceable/queue
  - POST /nonSourceable/{nonSourceabilityId}

- **Entity**: `dataland-qa-service/src/main/kotlin/com/d_fine/dataland/qas/nonsourceable/entity/NonSourceableQaReviewInformationEntity.kt`

- **Service**: `dataland-qa-service/src/main/kotlin/com/d_fine/dataland/qas/nonsourceable/service/NonSourceableQaService.kt`

- **Event Listener**: `dataland-qa-service/src/main/kotlin/com/d_fine/dataland/qas/nonsourceable/service/NonSourceableQaEventListener.kt`
  - Listens for non-sourceability-created events, creates review tasks

- **Event Publisher**: `dataland-qa-service/src/main/kotlin/com/d_fine/dataland/qas/nonsourceable/service/NonSourceableQaEventPublisher.kt`
  - Publishes QA decision events (accepted/rejected)

### Data-Sourcing Service (dataland-data-sourcing-service)

- **Event Listener**: `dataland-data-sourcing-service/src/main/kotlin/com/d_fine/dataland/datasourcing/state/service/NonSourceableEventListener.kt`
  - Listens for all non-sourceability events
  - Updates dataset state: NON_SOURCEABLE_VERIFICATION, NON_SOURCEABLE

- **State Service**: `dataland-data-sourcing-service/src/main/kotlin/com/d_fine/dataland/datasourcing/state/service/DataSourcingStateService.kt`
  - Implements state transitions with authorization checks

- **State Security**: `dataland-data-sourcing-service/src/main/kotlin/com/d_fine/dataland/datasourcing/state/security/DataSourcingStateSecurityService.kt`
  - Enforces `canUserPatchState()` rule: only admins can patch to NON_SOURCEABLE

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
grep "State transitioned to NON_SOURCEABLE" dataland-data-sourcing-service/logs/*.log
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
