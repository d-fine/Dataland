package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.NonSourceabilityInformationEntity
import org.dataland.datalandbackend.entities.SourceabilityEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.enums.commons.QaNonSourceabilityStatus
import org.dataland.datalandbackend.model.metainformation.SourceabilityInfo
import org.dataland.datalandbackend.model.metainformation.SourceabilityInfoResponse
import org.dataland.datalandbackend.repositories.NonSourceabilityDataRepository
import org.dataland.datalandbackend.repositories.SourceabilityDataRepository
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackend.repositories.utils.NonSourceableDataSearchFilter
import org.dataland.datalandbackend.utils.IdUtils.generateCorrelationId
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.NonSourceabilityAutoAcceptedEventPayload
import org.dataland.datalandmessagequeueutils.messages.NonSourceabilityCreatedEventPayload
import org.dataland.datalandmessagequeueutils.messages.QaNonSourceabilityAcceptedEventPayload
import org.dataland.datalandmessagequeueutils.messages.QaNonSourceabilityRejectedEventPayload
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID

/**
 * A service class for managing information about the sourceabilty of datasets.
 */
@Service("SourceabilityDataManager")
class SourceabilityDataManager(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val sourceabilityDataRepository: SourceabilityDataRepository,
    @Autowired private val nonSourceabilityDataRepository: NonSourceabilityDataRepository,
    @Autowired private val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired private val companyQueryManager: CompanyQueryManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * The method stores meta information to a non-sourceable dataset in the nonSourceableDataRepository
     * @param sourceabilityInfo the of the dataset
     */
    fun storeNonSourceableData(sourceabilityInfo: SourceabilityInfo): SourceabilityInfoResponse? {
        val now = Instant.now().toEpochMilli()
        val userId = DatalandAuthentication.fromContext().userId
        val qaStatus = if (sourceabilityInfo.bypassQa) QaNonSourceabilityStatus.Accepted else QaNonSourceabilityStatus.Pending
        val nonSourceabilityEntity =
            NonSourceabilityInformationEntity(
                nonSourceabilityId = null,
                companyId = sourceabilityInfo.companyId,
                dataType = sourceabilityInfo.dataType,
                reportingPeriod = sourceabilityInfo.reportingPeriod,
                reason = sourceabilityInfo.reason,
                uploaderUserId = userId,
                uploadTime = now,
                qaStatus = qaStatus,
                currentlyActive = sourceabilityInfo.bypassQa,
                bypassQa = sourceabilityInfo.bypassQa,
                createdAt = now,
                updatedAt = now,
            )
        return nonSourceabilityDataRepository.save(nonSourceabilityEntity).toApiModel()
    }

    /**
     * Processes a request to store information about a dataset being labeled as non-sourceable.
     * This includes verifying the existence of the company, checking if the dataset already exists,
     * storing the non-sourceable data, and sending a corresponding message to a message queue.
     * @param sourceabilityInfo the SourceabilityInfo of the dataset
     */
    @Transactional
    fun processSourceabilityDataStorageRequest(sourceabilityInfo: SourceabilityInfo) {
        val correlationId = generateCorrelationId(sourceabilityInfo.companyId, null)
        companyQueryManager.assertCompanyIdExists(sourceabilityInfo.companyId)

        validateNoActiveNonSourceabilityRequest(sourceabilityInfo, correlationId)
        validateNoAcceptedDataExists(sourceabilityInfo, correlationId)

        val storedNonSourceabilityInfo = requireNotNull(storeNonSourceableData(sourceabilityInfo))
        val eventData = buildEventData(storedNonSourceabilityInfo, sourceabilityInfo)

        publishNonSourceabilityEvent(eventData, sourceabilityInfo, correlationId)
    }

    private fun validateNoActiveNonSourceabilityRequest(
        sourceabilityInfo: SourceabilityInfo,
        correlationId: String,
    ) {
        val activeRequestExists =
            nonSourceabilityDataRepository.existsActiveNonSourceabilityRequest(
                sourceabilityInfo.companyId,
                sourceabilityInfo.dataType,
                sourceabilityInfo.reportingPeriod,
            )
        if (activeRequestExists) {
            logger.info(
                "Creating a NonSourceabilityInformationEntity failed because an active request already exists " +
                    "(correlationId: $correlationId)",
            )
            throw InvalidInputApiException(
                "Active non-sourceability request already exists for the given dimensions.",
                "Active non-sourceability request exists for companyId ${sourceabilityInfo.companyId}, " +
                    "reporting period ${sourceabilityInfo.reportingPeriod} and dataType ${sourceabilityInfo.dataType}.",
            )
        }
    }

    private fun validateNoAcceptedDataExists(
        sourceabilityInfo: SourceabilityInfo,
        correlationId: String,
    ) {
        val dataMetaInfo =
            dataMetaInformationManager.searchDataMetaInfo(
                DataMetaInformationSearchFilter(
                    companyId = sourceabilityInfo.companyId,
                    dataType = sourceabilityInfo.dataType,
                    reportingPeriod = sourceabilityInfo.reportingPeriod,
                    qaStatus = QaStatus.Accepted,
                    onlyActive = false,
                ),
            )
        if (dataMetaInfo.isNotEmpty()) {
            logger.info(
                "Creating a NonSourceabilityInformationEntity failed because an accepted dataset exists " +
                    "(correlationId: $correlationId)",
            )
            throw InvalidInputApiException(
                "DataMetaInfo exists for the triple companyId, reportingPeriod and dataType.",
                "DataMetaInfo exists for companyId ${sourceabilityInfo.companyId}, " + "reporting period " +
                    "${sourceabilityInfo.reportingPeriod} and dataType ${sourceabilityInfo.dataType}. ",
            )
        }
    }

    private data class EventData(
        val nonSourceabilityId: UUID,
        val eventId: UUID,
        val companyIdAsUuid: UUID,
        val uploadTime: ZonedDateTime,
        val publishedTime: ZonedDateTime,
        val uploaderUserId: String,
    )

    private fun buildEventData(
        storedNonSourceabilityInfo: SourceabilityInfoResponse,
        sourceabilityInfo: SourceabilityInfo,
    ): EventData {
        val nonSourceabilityId = storedNonSourceabilityInfo.nonSourceabilityId ?: UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val companyIdAsUuid =
            try {
                UUID.fromString(sourceabilityInfo.companyId)
            } catch (_: Exception) {
                UUID.nameUUIDFromBytes(sourceabilityInfo.companyId.toByteArray())
            }
        val uploadTimeMillis = storedNonSourceabilityInfo.uploadTime ?: storedNonSourceabilityInfo.creationTime
        val uploadTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(uploadTimeMillis), ZoneOffset.UTC)
        val publishedTime = ZonedDateTime.now(ZoneOffset.UTC)
        val uploaderUserId = storedNonSourceabilityInfo.uploaderUserId ?: storedNonSourceabilityInfo.userId

        return EventData(
            nonSourceabilityId = nonSourceabilityId,
            eventId = eventId,
            companyIdAsUuid = companyIdAsUuid,
            uploadTime = uploadTime,
            publishedTime = publishedTime,
            uploaderUserId = uploaderUserId,
        )
    }

    private fun publishNonSourceabilityEvent(
        eventData: EventData,
        sourceabilityInfo: SourceabilityInfo,
        correlationId: String,
    ) {
        if (sourceabilityInfo.bypassQa) {
            publishAutoAcceptedEvent(eventData, sourceabilityInfo, correlationId)
        } else {
            publishCreatedEvent(eventData, sourceabilityInfo, correlationId)
        }
    }

    private fun publishAutoAcceptedEvent(
        eventData: EventData,
        sourceabilityInfo: SourceabilityInfo,
        correlationId: String,
    ) {
        val payload =
            NonSourceabilityAutoAcceptedEventPayload(
                eventId = eventData.eventId,
                nonSourceabilityId = eventData.nonSourceabilityId,
                companyId = eventData.companyIdAsUuid,
                dataType = sourceabilityInfo.dataType.name,
                reportingPeriod = sourceabilityInfo.reportingPeriod,
                reason = sourceabilityInfo.reason,
                uploaderUserId = eventData.uploaderUserId,
                uploadTime = eventData.uploadTime,
                eventPublishedTime = eventData.publishedTime,
            )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body = objectMapper.writeValueAsString(payload),
            type = MessageType.NON_SOURCEABILITY_AUTO_ACCEPTED,
            correlationId = correlationId,
            exchange = ExchangeName.BACKEND_DATA_NONSOURCEABLE,
            routingKey = RoutingKeyNames.NON_SOURCEABILITY_AUTO_ACCEPTED,
        )
        logger.info(
            "Published event type ${MessageType.NON_SOURCEABILITY_AUTO_ACCEPTED} for nonSourceabilityId " +
                "${eventData.nonSourceabilityId} (correlationId: $correlationId)",
        )
    }

    private fun publishCreatedEvent(
        eventData: EventData,
        sourceabilityInfo: SourceabilityInfo,
        correlationId: String,
    ) {
        val payload =
            NonSourceabilityCreatedEventPayload(
                eventId = eventData.eventId,
                nonSourceabilityId = eventData.nonSourceabilityId,
                companyId = eventData.companyIdAsUuid,
                dataType = sourceabilityInfo.dataType.name,
                reportingPeriod = sourceabilityInfo.reportingPeriod,
                reason = sourceabilityInfo.reason,
                uploaderUserId = eventData.uploaderUserId,
                uploadTime = eventData.uploadTime,
                eventPublishedTime = eventData.publishedTime,
            )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body = objectMapper.writeValueAsString(payload),
            type = MessageType.NON_SOURCEABILITY_CREATED,
            correlationId = correlationId,
            exchange = ExchangeName.BACKEND_DATA_NONSOURCEABLE,
            routingKey = RoutingKeyNames.NON_SOURCEABILITY_CREATED,
        )
        logger.info(
            "Published event type ${MessageType.NON_SOURCEABILITY_CREATED} for nonSourceabilityId " +
                "${eventData.nonSourceabilityId} (correlationId: $correlationId)",
        )
    }

    /**
     * The method retrieves non-sourceable datasets by given filters.
     * @param companyId if not empty, filters the requested information by companyId.
     * @param dataType if not empty, filters the requested information by data type.
     * @param reportingPeriod if not empty, filters the requested information by reporting period.
     * @param nonSourceable if not null, filters the requested information to include only datasets
     *                      with a non-sourceable flag matching the provided value (true or false).
     * @return a list of SourceabilityInfoResponse objects that match the specified filters.
     */
    fun getSourceabilityDataByFilters(
        companyId: String?,
        dataType: DataType?,
        reportingPeriod: String?,
        nonSourceable: Boolean?,
    ): List<SourceabilityInfoResponse> {
        val nonSourceabilityEntities =
            nonSourceabilityDataRepository.findByDimensions(
                companyId = companyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
            )

        if (nonSourceabilityEntities.isNotEmpty()) {
            val mapped = nonSourceabilityEntities.map { it.toApiModel() }
            return if (nonSourceable == null) mapped else mapped.filter { it.isNonSourceable == nonSourceable }
        }

        val legacySourceabilityEntities =
            sourceabilityDataRepository.searchNonSourceableData(
                NonSourceableDataSearchFilter(
                    companyId,
                    dataType,
                    reportingPeriod,
                    nonSourceable,
                ),
            )
        return legacySourceabilityEntities.map { it.toApiModel() }
    }

    /**
     * Gets the latest non-sourceable info for triple (companyId, dataType, reportingPeriod)
     * @param companyId companyId
     * @param dataType dataType
     * @param reportingPeriod reportingPeriod
     * @return most recent SourceabilityInfoResponse for triple (companyId, dataType, reportingPeriod)
     */
    fun getLatestSourceabilityInfoForDataset(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
    ): SourceabilityInfoResponse? =
        nonSourceabilityDataRepository.findLatestByDimensions(companyId, dataType, reportingPeriod)?.toApiModel()
            ?: sourceabilityDataRepository
                .getLatestSourceabilityInfoForDataset(
                    NonSourceableDataSearchFilter(
                        companyId,
                        dataType,
                        reportingPeriod,
                    ),
                )?.toApiModel()

    /**
     * Stores a NonSourceableEntity in the data-sourceability table, marking the previously
     * non-sourceable dataset as sourceable. This is triggered by the upload of the dataset.
     *
     * @param companyId the ID of the company associated with the dataset.
     * @param dataType the type of the dataset being uploaded.
     * @param reportingPeriod the reporting period of the dataset, typically a specific year or quarter.
     * @param uploaderId the ID of the user who uploaded the dataset, used for audit purposes.
     */
    fun storeSourceableData(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
        uploaderId: String,
    ): SourceabilityInfoResponse? {
        val activeRequest = nonSourceabilityDataRepository.findActiveRequest(companyId, dataType, reportingPeriod)
        if (activeRequest != null && activeRequest.currentlyActive) {
            activeRequest.currentlyActive = false
            activeRequest.updatedAt = Instant.now().toEpochMilli()
            nonSourceabilityDataRepository.save(activeRequest)
        }

        val creationTime = Instant.now().toEpochMilli()

        val sourceabilityEntity =
            SourceabilityEntity(
                eventId = null,
                companyId = companyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                isNonSourceable = false,
                reason = "Uploaded by a user with the Id:$uploaderId",
                creationTime = creationTime,
                userId = uploaderId,
            )
        return sourceabilityDataRepository.save(sourceabilityEntity).toApiModel()
    }

    /**
     * Applies an accepted QA decision event for a non-sourceability request.
     */
    @Transactional
    fun processQaNonSourceabilityAcceptedEvent(
        payload: QaNonSourceabilityAcceptedEventPayload,
        correlationId: String,
    ) {
        val reviewItem = nonSourceabilityDataRepository.findById(payload.nonSourceabilityId).orElse(null)
        if (reviewItem == null) {
            logger.warn(
                "Could not process QA accepted event because non-sourceability request ${payload.nonSourceabilityId} " +
                    "was not found (correlationId: $correlationId)",
            )
            return
        }

        reviewItem.qaStatus = QaNonSourceabilityStatus.Accepted
        reviewItem.currentlyActive = true
        reviewItem.updatedAt = Instant.now().toEpochMilli()
        nonSourceabilityDataRepository.save(reviewItem)

        logger.info(
            "Processed QA accepted event for nonSourceabilityId ${payload.nonSourceabilityId}; " +
                "set qaStatus=${QaNonSourceabilityStatus.Accepted} and currentlyActive=true (correlationId: $correlationId)",
        )
    }

    /**
     * Applies a rejected QA decision event for a non-sourceability request.
     */
    @Transactional
    fun processQaNonSourceabilityRejectedEvent(
        payload: QaNonSourceabilityRejectedEventPayload,
        correlationId: String,
    ) {
        val reviewItem = nonSourceabilityDataRepository.findById(payload.nonSourceabilityId).orElse(null)
        if (reviewItem == null) {
            logger.warn(
                "Could not process QA rejected event because non-sourceability request ${payload.nonSourceabilityId} " +
                    "was not found (correlationId: $correlationId)",
            )
            return
        }

        reviewItem.qaStatus = QaNonSourceabilityStatus.Rejected
        reviewItem.currentlyActive = false
        reviewItem.updatedAt = Instant.now().toEpochMilli()
        nonSourceabilityDataRepository.save(reviewItem)

        logger.info(
            "Processed QA rejected event for nonSourceabilityId ${payload.nonSourceabilityId}; " +
                "set qaStatus=${QaNonSourceabilityStatus.Rejected} and currentlyActive=false (correlationId: $correlationId)",
        )
    }
}
