package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.NonSourceabilityInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.NonSourceabilityInformationResponse
import org.dataland.datalandbackend.model.metainformation.NonSourceabilityRequest
import org.dataland.datalandbackend.repositories.NonSourceabilityDataRepository
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.logging.CorrelationLogging
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityEventType
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Manages the canonical non-sourceability lifecycle in the backend.
 *
 * This service is the single source of truth for non-sourceability state (FR-011, SC-005).
 */
@Service
class NonSourceabilityInformationManager(
    @Autowired private val nonSourceabilityDataRepository: NonSourceabilityDataRepository,
    @Autowired private val companyQueryManager: CompanyQueryManager,
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Processes a non-sourceability submission request. Validates uniqueness, persists the entry,
     * and emits the appropriate lifecycle event (FR-001, FR-002, FR-003).
     */
    @Transactional
    fun processNonSourceabilityRequest(request: NonSourceabilityRequest): NonSourceabilityInformationResponse {
        companyQueryManager.assertCompanyIdExists(request.companyId)

        val blockedStatuses = listOf(QaStatus.Pending, QaStatus.Accepted)
        val hasDuplicate =
            nonSourceabilityDataRepository.existsActiveOrPendingForTuple(
                request.companyId,
                request.dataType,
                request.reportingPeriod,
                blockedStatuses,
            )
        if (hasDuplicate) {
            throw InvalidInputApiException(
                summary = "Duplicate non-sourceability entry.",
                message =
                    "An active or pending non-sourceability entry already exists for " +
                        "companyId=${request.companyId}, dataType=${request.dataType}, " +
                        "reportingPeriod=${request.reportingPeriod}.",
            )
        }

        val userId = DatalandAuthentication.fromContext().userId
        val uploadTime = Instant.now().toEpochMilli()
        val qaStatus = if (request.bypassQa) QaStatus.Accepted else QaStatus.Pending
        val currentlyActive = request.bypassQa

        val entity =
            NonSourceabilityInformationEntity(
                companyId = request.companyId,
                dataType = request.dataType,
                reportingPeriod = request.reportingPeriod,
                qaStatus = qaStatus,
                uploaderUserId = userId,
                uploadTime = uploadTime,
                currentlyActive = currentlyActive,
                reason = request.reason,
                bypassQa = request.bypassQa,
            )

        val saved = nonSourceabilityDataRepository.save(entity)
        val nonSourceabilityId = saved.nonSourceabilityId.toString()
        val correlationId = nonSourceabilityId

        CorrelationLogging.withNonSourceabilityContext(correlationId, nonSourceabilityId) {
            emitLifecycleEvent(saved, request.bypassQa, correlationId)
        }

        logger.info(
            "NonSourceabilityInformation persisted with id=$nonSourceabilityId, " +
                "bypassQa=${request.bypassQa}, qaStatus=$qaStatus (correlationId=$correlationId)",
        )

        return saved.toResponse()
    }

    /**
     * Returns entries matching the provided optional filters (GET /metadata/nonSourceable).
     */
    fun getByFilters(
        companyId: String?,
        dataType: DataType?,
        reportingPeriod: String?,
        qaStatus: QaStatus?,
    ): List<NonSourceabilityInformationResponse> =
        nonSourceabilityDataRepository
            .findByFilters(companyId, dataType, reportingPeriod, qaStatus)
            .map { it.toResponse() }

    /**
     * Returns true if an active non-sourceability entry exists for the given tuple
     * (used by HEAD /metadata/nonSourceable/{companyId}/{dataType}/{reportingPeriod}).
     */
    fun isCurrentlyActive(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
    ): Boolean =
        nonSourceabilityDataRepository
            .findActiveForTuple(companyId, dataType, reportingPeriod)
            .isNotEmpty()

    /**
     * Sets currentlyActive = false on every active non-sourceability entry for the given triple.
     * Called when a real dataset is QA-accepted for the same (companyId, dataType, reportingPeriod),
     * meaning data now exists and the non-sourceability is no longer valid.
     */
    @Transactional
    fun deactivateForTriple(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
    ) {
        val active = nonSourceabilityDataRepository.findActiveForTuple(companyId, dataType, reportingPeriod)
        if (active.isNotEmpty()) {
            active.forEach { it.currentlyActive = false }
            nonSourceabilityDataRepository.saveAll(active)
            logger.info(
                "Deactivated ${active.size} non-sourceability entries for " +
                    "companyId=$companyId, dataType=$dataType, reportingPeriod=$reportingPeriod",
            )
        }
    }

    private fun emitLifecycleEvent(
        entity: NonSourceabilityInformationEntity,
        bypassQa: Boolean,
        correlationId: String,
    ) {
        val (eventType, routingKey) =
            if (bypassQa) {
                Pair(NonSourceabilityEventType.NON_SOURCEABILITY_AUTO_ACCEPTED, RoutingKeyNames.NON_SOURCEABILITY_AUTO_ACCEPTED)
            } else {
                Pair(NonSourceabilityEventType.NON_SOURCEABILITY_CREATED, RoutingKeyNames.NON_SOURCEABILITY_CREATED)
            }

        val event =
            NonSourceabilityLifecycleEvent(
                nonSourceabilityId = entity.nonSourceabilityId.toString(),
                companyId = entity.companyId,
                dataType = entity.dataType.name,
                reportingPeriod = entity.reportingPeriod,
                eventType = eventType,
            )

        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body = objectMapper.writeValueAsString(event),
            type = if (bypassQa) MessageType.NON_SOURCEABILITY_AUTO_ACCEPTED else MessageType.NON_SOURCEABILITY_CREATED,
            correlationId = correlationId,
            exchange = ExchangeName.BACKEND_DATA_NONSOURCEABLE,
            routingKey = routingKey,
        )
    }

    private fun NonSourceabilityInformationEntity.toResponse(): NonSourceabilityInformationResponse =
        NonSourceabilityInformationResponse(
            nonSourceabilityId = nonSourceabilityId.toString(),
            companyId = companyId,
            dataType = dataType,
            reportingPeriod = reportingPeriod,
            qaStatus = qaStatus,
            uploaderUserId = uploaderUserId,
            uploadTime = uploadTime,
            currentlyActive = currentlyActive,
            reason = reason,
            bypassQa = bypassQa,
        )
}
