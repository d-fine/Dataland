package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.NonSourceabilityInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.NonSourceabilityInformationResponse
import org.dataland.datalandbackend.model.metainformation.NonSourceabilityRequest
import org.dataland.datalandbackend.repositories.NonSourceabilityDataRepository
import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.time.Instant

/**
 * Manages the non-sourceability workflow in the backend.
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
     * Processes a non-sourceability submission request.
     *
     * Routes to one of four cases based on (bypassQa, currentlyActive):
     * - (false, true) → rejected immediately (invalid combination)
     * - (false, false) → standard QA path: creates Pending entry, emits NON_SOURCEABILITY_CREATED Event
     * - (true, true) → admin bypass: creates Accepted+active entry, emits NON_SOURCEABILITY_AUTO_ACCEPTED Event
     * - (true, false) → admin reversal: deactivates active entry, creates audit entry, no event
     */
    sealed class ProcessNonSourceabilityResult {
        /**
         * Represents a successful non-sourceability request result.
         */
        data class Success(
            val response: NonSourceabilityInformationResponse,
        ) : ProcessNonSourceabilityResult()
    }

    /**
     * Processes a non-sourceability submission request for the given request.
     *
     * Routes to one of four cases based on bypassQa and currentlyActive:
     * false, true → throws InvalidInputApiException immediately — invalid combination.
     * false, false → standard QA path: creates a Pending entry, emits NON_SOURCEABILITY_CREATED.
     * true, true → admin bypass: creates an Accepted + active entry, emits NON_SOURCEABILITY_AUTO_ACCEPTED.
     * true, false → admin reversal: deactivates the existing active entry, creates an audit entry, no event emitted.
     *
     * @param request the non-sourceability submission containing companyId, dataType, reportingPeriod,
     * reason, bypassQa flag, and currentlyActive flag.
     * @return a Success wrapping the persisted NonSourceabilityInformationResponse.
     * @throws InvalidInputApiException if bypassQa=false and currentlyActive=true.
     * @throws ConflictApiException if a pending entry already exists for the same (companyId, dataType, reportingPeriod) triple;
     * if bypassQa=true, currentlyActive=true and an active entry already exists;
     * or if bypassQa=true, currentlyActive=fals` and no active entry exists to reverse.
     */
    @Transactional
    fun processNonSourceabilityRequest(request: NonSourceabilityRequest): ProcessNonSourceabilityResult.Success {
        companyQueryManager.assertCompanyIdExists(request.companyId)

        if (!request.bypassQa && request.currentlyActive) {
            throw InvalidInputApiException(
                summary = "Invalid non-sourceability request.",
                message =
                    "currentlyActive=true requires bypassQa=true. " +
                        "The QA service is responsible for setting currentlyActive=true on QA-reviewed entries.",
            )
        }

        val hasPendingEntry =
            nonSourceabilityDataRepository.existsActiveOrPendingForTuple(
                request.companyId,
                request.dataType,
                request.reportingPeriod,
                listOf(QaStatus.Pending),
            )
        if (hasPendingEntry) {
            throw ConflictApiException(
                summary = "Pending non-sourceability entry exists.",
                message =
                    "A pending non-sourceability entry already exists for " +
                        "companyId=${request.companyId}, dataType=${request.dataType}, " +
                        "reportingPeriod=${request.reportingPeriod}. Resolve the pending entry first.",
            )
        }

        return if (request.bypassQa && !request.currentlyActive) {
            processReversal(request)
        } else {
            processStandardOrBypassCreate(request)
        }
    }

    /**
     * Handles bypassQa=true, currentlyActive=false: marks the triple as sourceable again.
     * Deactivates the existing active entry and creates a new audit entry. No event is emitted.
     */
    private fun processReversal(request: NonSourceabilityRequest): ProcessNonSourceabilityResult.Success {
        val activeEntries =
            nonSourceabilityDataRepository.findActiveForTuple(
                request.companyId,
                request.dataType,
                request.reportingPeriod,
            )
        if (activeEntries.isEmpty()) {
            throw ConflictApiException(
                summary = "Triple is already sourceable.",
                message =
                    "No active non-sourceability entry exists for " +
                        "companyId=${request.companyId}, dataType=${request.dataType}, " +
                        "reportingPeriod=${request.reportingPeriod}. The triple is already sourceable.",
            )
        }

        activeEntries.forEach { it.currentlyActive = false }
        nonSourceabilityDataRepository.saveAll(activeEntries)

        val userId = DatalandAuthentication.fromContext().userId
        val saved =
            nonSourceabilityDataRepository.save(
                NonSourceabilityInformationEntity(
                    companyId = request.companyId,
                    dataType = request.dataType,
                    reportingPeriod = request.reportingPeriod,
                    qaStatus = QaStatus.Accepted,
                    uploaderUserId = userId,
                    uploadTime = Instant.now().toEpochMilli(),
                    currentlyActive = false,
                    reason = request.reason,
                    bypassQa = true,
                ),
            )
        logger.info(
            "Non-sourceability reversal: deactivated ${activeEntries.size} active entry(entries) and " +
                "created audit entry ${saved.nonSourceabilityId} for " +
                "companyId=${request.companyId}, dataType=${request.dataType}, reportingPeriod=${request.reportingPeriod}",
        )
        return ProcessNonSourceabilityResult.Success(saved.toResponse())
    }

    /**
     * Handles bypassQa=false/currentlyActive=false (standard QA) and
     * bypassQa=true/currentlyActive=true (admin bypass mark as non-sourceable).
     * Creates a new entry and emits the appropriate lifecycle event.
     */
    private fun processStandardOrBypassCreate(request: NonSourceabilityRequest): ProcessNonSourceabilityResult.Success {
        val hasActiveEntry =
            nonSourceabilityDataRepository
                .findActiveForTuple(
                    request.companyId,
                    request.dataType,
                    request.reportingPeriod,
                ).isNotEmpty()
        if (hasActiveEntry) {
            throw ConflictApiException(
                summary = "Active non-sourceability entry exists.",
                message =
                    "An active non-sourceability entry already exists for " +
                        "companyId=${request.companyId}, dataType=${request.dataType}, " +
                        "reportingPeriod=${request.reportingPeriod}.",
            )
        }

        val userId = DatalandAuthentication.fromContext().userId
        val qaStatus = if (request.bypassQa) QaStatus.Accepted else QaStatus.Pending
        val entity =
            NonSourceabilityInformationEntity(
                companyId = request.companyId,
                dataType = request.dataType,
                reportingPeriod = request.reportingPeriod,
                qaStatus = qaStatus,
                uploaderUserId = userId,
                uploadTime = Instant.now().toEpochMilli(),
                currentlyActive = request.currentlyActive,
                reason = request.reason,
                bypassQa = request.bypassQa,
            )

        val saved = nonSourceabilityDataRepository.save(entity)
        val nonSourceabilityId = saved.nonSourceabilityId.toString()

        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    emitLifecycleEvent(saved, request.bypassQa, nonSourceabilityId)
                }
            },
        )
        logger.info(
            "NonSourceabilityInformation persisted with id=$nonSourceabilityId, " +
                "bypassQa=${request.bypassQa}, qaStatus=$qaStatus (correlationId=$nonSourceabilityId)",
        )
        return ProcessNonSourceabilityResult.Success(saved.toResponse())
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
    fun isTripleCurrentlyNonSourceable(
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
    fun deactivateExistingNonSourceabilitiesForTriple(
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
        val event =
            NonSourceabilityLifecycleEvent(
                nonSourceabilityId = entity.nonSourceabilityId.toString(),
                companyId = entity.companyId,
                dataType = entity.dataType.name,
                reportingPeriod = entity.reportingPeriod,
                uploaderUserId = entity.uploaderUserId,
            )

        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body = objectMapper.writeValueAsString(event),
            type = if (bypassQa) MessageType.NON_SOURCEABILITY_AUTO_ACCEPTED else MessageType.NON_SOURCEABILITY_CREATED,
            correlationId = correlationId,
            exchange = ExchangeName.BACKEND_DATA_NONSOURCEABLE,
            routingKey = RoutingKeyNames.NON_SOURCEABILITY_SUBMISSION,
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
