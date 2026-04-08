package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.NonSourceabilityInformationEntity
import org.dataland.datalandbackend.entities.SourceabilityEntity
import org.dataland.datalandbackend.model.DataType
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
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityEventType
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

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

    private fun requireAdminRoleForBypassQa(bypassQa: Boolean) {
        if (!bypassQa) {
            return
        }
        val user = DatalandAuthentication.fromContextOrNull()
        if (user == null || DatalandRealmRole.ROLE_ADMIN !in user.roles) {
            throw AccessDeniedException("bypassQa=true requires admin privileges")
        }
    }

    private fun NonSourceabilityInformationEntity.toApiModel(): SourceabilityInfoResponse =
        SourceabilityInfoResponse(
            nonSourceabilityId = nonSourceabilityId?.toString(),
            companyId = companyId,
            dataType = dataType,
            reportingPeriod = reportingPeriod,
            isNonSourceable = currentlyActive,
            qaStatus = qaStatus,
            currentlyActive = currentlyActive,
            bypassQa = bypassQa,
            reason = reason,
            creationTime = uploadTime,
            userId = uploaderUserId,
        )

    /**
     * Stores non-sourceable data and publishes lifecycle events.
     *
     * @param sourceabilityInfo the sourceability information to store
     * @param bypassQa whether QA review should be bypassed
     * @return the stored sourceability information as a response object
     */
    @Transactional
    fun storeNonSourceableData(
        sourceabilityInfo: SourceabilityInfo,
        bypassQa: Boolean,
    ): SourceabilityInfoResponse {
        val creationTime = Instant.now().toEpochMilli()
        val userId = DatalandAuthentication.fromContext().userId
        val qaStatus = if (bypassQa) QaStatus.Accepted else QaStatus.Pending
        val currentlyActive = bypassQa

        val canonicalEntity =
            nonSourceabilityDataRepository.save(
                createCanonicalEntity(
                    sourceabilityInfo = sourceabilityInfo,
                    qaStatus = qaStatus,
                    userId = userId,
                    creationTime = creationTime,
                    currentlyActive = currentlyActive,
                    bypassQa = bypassQa,
                ),
            )

        // Keep legacy sourceability table as backup-only history.
        sourceabilityDataRepository.save(
            createLegacySourceabilityEntity(
                sourceabilityInfo = sourceabilityInfo,
                creationTime = creationTime,
                userId = userId,
            ),
        )

        val nonSourceabilityId = canonicalEntity.nonSourceabilityId?.toString()
        require(!nonSourceabilityId.isNullOrBlank()) { "Saved canonical non-sourceability id must not be null" }

        val eventType = if (bypassQa) NonSourceabilityEventType.AUTO_ACCEPTED else NonSourceabilityEventType.CREATED
        val correlationId = generateCorrelationId(sourceabilityInfo.companyId, null)

        publishLifecycleEvents(
            canonicalEntity = canonicalEntity,
            nonSourceabilityId = nonSourceabilityId,
            eventType = eventType,
            correlationId = correlationId,
        )

        return canonicalEntity.toApiModel()
    }

    private fun createCanonicalEntity(
        sourceabilityInfo: SourceabilityInfo,
        qaStatus: QaStatus,
        userId: String,
        creationTime: Long,
        currentlyActive: Boolean,
        bypassQa: Boolean,
    ): NonSourceabilityInformationEntity =
        NonSourceabilityInformationEntity(
            companyId = sourceabilityInfo.companyId,
            dataType = sourceabilityInfo.dataType,
            reportingPeriod = sourceabilityInfo.reportingPeriod,
            qaStatus = qaStatus,
            uploaderUserId = userId,
            uploadTime = creationTime,
            currentlyActive = currentlyActive,
            reason = sourceabilityInfo.reason,
            bypassQa = bypassQa,
        )

    private fun createLegacySourceabilityEntity(
        sourceabilityInfo: SourceabilityInfo,
        creationTime: Long,
        userId: String,
    ): SourceabilityEntity =
        SourceabilityEntity(
            eventId = null,
            companyId = sourceabilityInfo.companyId,
            dataType = sourceabilityInfo.dataType,
            reportingPeriod = sourceabilityInfo.reportingPeriod,
            isNonSourceable = true,
            reason = sourceabilityInfo.reason,
            creationTime = creationTime,
            userId = userId,
        )

    private fun publishLifecycleEvents(
        canonicalEntity: NonSourceabilityInformationEntity,
        nonSourceabilityId: String,
        eventType: NonSourceabilityEventType,
        correlationId: String,
    ) {
        val lifecycleEvent = createLifecycleEvent(canonicalEntity, nonSourceabilityId, eventType)
        val legacyEventPayload = createLegacyEventPayload(canonicalEntity, nonSourceabilityId, eventType)

        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body = objectMapper.writeValueAsString(legacyEventPayload),
            type = MessageType.DATA_NONSOURCEABLE,
            correlationId = correlationId,
            exchange = ExchangeName.BACKEND_DATA_NONSOURCEABLE,
            routingKey = RoutingKeyNames.DATA_NONSOURCEABLE,
        )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body = objectMapper.writeValueAsString(lifecycleEvent),
            type = MessageType.NON_SOURCEABILITY_LIFECYCLE,
            correlationId = correlationId,
            exchange = ExchangeName.BACKEND_DATA_NONSOURCEABLE,
            routingKey = RoutingKeyNames.NON_SOURCEABILITY_LIFECYCLE,
        )
    }

    private fun createLifecycleEvent(
        canonicalEntity: NonSourceabilityInformationEntity,
        nonSourceabilityId: String,
        eventType: NonSourceabilityEventType,
    ): NonSourceabilityLifecycleEvent =
        NonSourceabilityLifecycleEvent(
            nonSourceabilityId = nonSourceabilityId,
            companyId = canonicalEntity.companyId,
            dataType = canonicalEntity.dataType.toString(),
            reportingPeriod = canonicalEntity.reportingPeriod,
            eventType = eventType,
            isNonSourceable = true,
            qaStatus = canonicalEntity.qaStatus,
            currentlyActive = canonicalEntity.currentlyActive,
            reason = canonicalEntity.reason,
            bypassQa = canonicalEntity.bypassQa,
            uploaderUserId = canonicalEntity.uploaderUserId,
            uploadTime = canonicalEntity.uploadTime,
        )

    private fun createLegacyEventPayload(
        canonicalEntity: NonSourceabilityInformationEntity,
        nonSourceabilityId: String,
        eventType: NonSourceabilityEventType,
    ): SourceabilityInfo =
        SourceabilityInfo(
            companyId = canonicalEntity.companyId,
            dataType = canonicalEntity.dataType,
            reportingPeriod = canonicalEntity.reportingPeriod,
            isNonSourceable = true,
            reason = canonicalEntity.reason,
            nonSourceabilityId = nonSourceabilityId,
            qaStatus = canonicalEntity.qaStatus,
            currentlyActive = canonicalEntity.currentlyActive,
            bypassQa = canonicalEntity.bypassQa,
            eventType = eventType,
            uploaderUserId = canonicalEntity.uploaderUserId,
            uploadTime = canonicalEntity.uploadTime,
        )

    /**
     * Processes a request to store information about a dataset being labeled as non-sourceable.
     * This includes verifying the existence of the company, checking if the dataset already exists,
     * storing the non-sourceable data, and sending a corresponding message to a message queue.
     * @param sourceabilityInfo the SourceabilityInfo of the dataset
     */
    fun processSourceabilityDataStorageRequest(
        sourceabilityInfo: SourceabilityInfo,
        bypassQa: Boolean = false,
    ) {
        val correlationId = generateCorrelationId(sourceabilityInfo.companyId, null)
        companyQueryManager.assertCompanyIdExists(sourceabilityInfo.companyId)
        requireAdminRoleForBypassQa(bypassQa)

        validateNonSourceableRequest(sourceabilityInfo)
        checkExistingDataMetaInfo(sourceabilityInfo, correlationId)
        checkDuplicateNonSourceability(sourceabilityInfo, correlationId)

        val saved = storeNonSourceableData(sourceabilityInfo, bypassQa)
        logger.info(
            "Canonical non-sourceability entry ${saved.nonSourceabilityId} has been persisted with correlationId $correlationId",
        )
    }

    private fun validateNonSourceableRequest(sourceabilityInfo: SourceabilityInfo) {
        if (!sourceabilityInfo.isNonSourceable) {
            throw InvalidInputApiException(
                summary = "Only non-sourceable requests are accepted.",
                message = "The field isNonSourceable must be true for /metadata/nonSourceable requests.",
            )
        }
    }

    private fun checkExistingDataMetaInfo(
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
            val errorMessage =
                "Creating a NonSourceableEntity failed because the dataset exists (correlationId: $correlationId)"
            logger.info(errorMessage)
            throw InvalidInputApiException(
                "DataMetaInfo exists for the triple companyId, reportingPeriod and datyType. ",
                "DataMetaInfo exists for companyId ${sourceabilityInfo.companyId}, " +
                    "reporting period ${sourceabilityInfo.reportingPeriod} and " +
                    "dataType ${sourceabilityInfo.dataType}. ",
            )
        }
    }

    private fun checkDuplicateNonSourceability(
        sourceabilityInfo: SourceabilityInfo,
        correlationId: String,
    ) {
        val duplicateExists =
            nonSourceabilityDataRepository.existsByCompanyIdAndDataTypeAndReportingPeriodAndQaStatusIn(
                companyId = sourceabilityInfo.companyId,
                dataType = sourceabilityInfo.dataType,
                reportingPeriod = sourceabilityInfo.reportingPeriod,
                qaStatus = listOf(QaStatus.Pending, QaStatus.Accepted),
            )

        if (duplicateExists) {
            logger.info(
                "Rejecting duplicate non-sourceability request for tuple " +
                    "(${sourceabilityInfo.companyId}, ${sourceabilityInfo.dataType}, ${sourceabilityInfo.reportingPeriod}) " +
                    "(correlationId: $correlationId)",
            )
            throw InvalidInputApiException(
                summary = "Active or pending non-sourceability record already exists.",
                message =
                    "A non-sourceability request for companyId ${sourceabilityInfo.companyId}, " +
                        "dataType ${sourceabilityInfo.dataType}, and reportingPeriod ${sourceabilityInfo.reportingPeriod} already exists.",
            )
        }
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
        qaStatus: QaStatus? = null,
    ): List<SourceabilityInfoResponse> {
        val sourceabilityEntities =
            nonSourceabilityDataRepository.searchNonSourceabilityInformation(
                NonSourceableDataSearchFilter(
                    companyId = companyId,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    qaStatus = qaStatus,
                    currentlyActive = nonSourceable,
                ),
            )
        return sourceabilityEntities.map { it.toApiModel() }
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
        nonSourceabilityDataRepository
            .findFirstByCompanyIdAndDataTypeAndReportingPeriodOrderByUploadTimeDesc(companyId, dataType, reportingPeriod)
            ?.toApiModel()

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
}
