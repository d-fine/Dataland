@file:Suppress("TooManyFunctions")

package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.ExceptionForwarder
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.QaBypass
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.QaReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.toQaReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.QaSearchFilter
import org.dataland.datalandqaservice.repositories.QaReviewRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * A service class for managing QA report meta-information
 */
@Service
class QaReviewManager(
    @Autowired val qaReviewRepository: QaReviewRepository,
    @Autowired val companyDataControllerApi: CompanyDataControllerApi,
    @Autowired val metaDataControllerApi: MetaDataControllerApi,
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var objectMapper: ObjectMapper,
    @Autowired val exceptionForwarder: ExceptionForwarder,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Add a new qa review entry corresponding to a data set event (upload, qa status change, etc) to the qa review
     * history
     * @param dataId identifier of the dataset
     * @param bypassQa whether to bypass the qa process or not; if true, qa status of dataset is automatically set to
     * Accepted
     * @param correlationId
     */
    @Transactional
    fun addDatasetToQaReviewRepository(
        dataId: String,
        bypassQa: Boolean,
        correlationId: String,
    ) {
        logger.info("Received data with dataId $dataId and bypassQA $bypassQa on QA message queue (correlation Id: $correlationId)")
        val triggeringUserId = requireNotNull(metaDataControllerApi.getDataMetaInfo(dataId).uploaderUserId)
        val (qaStatus, comment) = QaBypass.getCommentAndStatusForBypass(bypassQa)

        val qaReviewEntity =
            saveQaReviewEntity(
                dataId = dataId,
                qaStatus = qaStatus,
                triggeringUserId = triggeringUserId,
                comment = comment,
                correlationId = correlationId,
            )

        this.sendQaStatusUpdateMessage(
            qaReviewEntity = qaReviewEntity, correlationId = correlationId,
        )
    }

    /**
     * The method returns a list of unreviewed datasets with corresponding information for the specified input params
     * @param dataTypes the datatype of the dataset
     * @param reportingPeriods the reportingPeriod of the dataset
     * @param companyName the company name connected to the dataset
     * @param chunkIndex the chunkIndex of the request
     * @param chunkSize the chunkSize of the request
     */
    @Transactional(readOnly = true)
    fun getInfoOnDatasets(
        dataTypes: Set<DataTypeEnum>?,
        reportingPeriods: Set<String>?,
        companyName: String?,
        qaStatus: QaStatus = QaStatus.Pending,
        chunkSize: Int,
        chunkIndex: Int,
    ): List<QaReviewResponse> {
        val offset = (chunkIndex) * (chunkSize)
        val userIsAdmin = DatalandAuthentication.fromContext().roles.contains(DatalandRealmRole.ROLE_ADMIN)
        return qaReviewRepository
            .getSortedAndFilteredQaReviewMetadataSet(
                QaSearchFilter(
                    dataTypes = dataTypes,
                    reportingPeriods = reportingPeriods,
                    companyIds = getCompanyIdsForCompanyName(companyName),
                    companyName = companyName,
                    qaStatuses = setOf(qaStatus),
                ),
                resultOffset = offset,
                resultLimit = chunkSize,
            ).map { it.toQaReviewResponse(userIsAdmin) }
    }

    /**
     * This method returns the number of unreviewed datasets for a specific set of filters
     * @param dataTypes the set of datatypes for which should be filtered
     * @param reportingPeriods the set of reportingPeriods for which should be filtered
     * @param companyName the companyName for which should be filtered
     */
    @Transactional
    fun getNumberOfPendingDatasets(
        dataTypes: Set<DataTypeEnum>?,
        reportingPeriods: Set<String>?,
        companyName: String?,
    ): Int =
        qaReviewRepository.getNumberOfFilteredQaReviews(
            QaSearchFilter(
                dataTypes = dataTypes, companyName = companyName, reportingPeriods = reportingPeriods,
                companyIds = getCompanyIdsForCompanyName(companyName), qaStatuses = setOf(QaStatus.Pending),
            ),
        )

    /**
     * Return the most recent Qa review entity for a particular data ID
     * @param dataId the data ID for which the information is retrieved
     */
    @Transactional
    fun getMostRecentQaReviewEntity(dataId: String): QaReviewEntity? = qaReviewRepository.findFirstByDataIdOrderByTimestampDesc(dataId)

    /**
     * Retrieves from database a QaReviewEntity by its dataId
     * @param dataId: dataID
     */
    @Transactional(readOnly = true)
    fun getQaReviewResponseByDataId(dataId: UUID): QaReviewResponse? {
        val userIsAdmin = DatalandAuthentication.fromContext().roles.contains(DatalandRealmRole.ROLE_ADMIN)
        return getMostRecentQaReviewEntity(dataId.toString())?.toQaReviewResponse(userIsAdmin)
    }

    /**
     * Method called to update triggeringUserId for first Pending entry of a dataset.
     * This effectively updates the uploaderUserId of the dataset. Method is only triggered via messageQueue.
     * Due to Spring JPA, the object is saved when the transaction is committed without explicitly calling save().
     * @param dataId identifier of dataset
     * @param uploaderUserId the new uploaderUserId aka the triggeringUserId of the upload event
     * @param correlationId
     */
    @Transactional
    fun patchUploaderUserIdInQaReviewEntry(
        dataId: String,
        uploaderUserId: String,
        correlationId: String,
    ) {
        logger.info("Received message to patch uploaderUserId for dataset with dataId $dataId (correlationId: $correlationId).")
        val qaReviewEntity = qaReviewRepository.findFirstByDataIdOrderByTimestampAsc(dataId)

        requireNotNull(qaReviewEntity, { "QaReviewEntity must not be null." })

        logger.info(
            "Updating triggeringUserId for first qa review entry for dataset with dataId $dataId$. " +
                "Old triggeringUserId was ${qaReviewEntity.triggeringUserId}, new triggeringUserId is $uploaderUserId " +
                "(correlationId: $correlationId).",
        )

        qaReviewEntity.triggeringUserId = uploaderUserId
    }

    /**
     * Saves QaReviewEntity to database
     * @param dataId dataId of dataset of which to change qaStatus
     * @param qaStatus new qaStatus to be set
     * @param triggeringUserId keycloakId of user triggering QA Status change or upload event
     * @param correlationId the ID for the process triggering the change
     */
    @Transactional
    fun saveQaReviewEntity(
        dataId: String,
        qaStatus: QaStatus,
        triggeringUserId: String,
        comment: String?,
        correlationId: String,
    ): QaReviewEntity {
        val dataMetaInfo = metaDataControllerApi.getDataMetaInfo(dataId)
        val companyName = companyDataControllerApi.getCompanyById(dataMetaInfo.companyId).companyInformation.companyName

        logger.info("Assigning quality status ${qaStatus.name} to dataset with ID $dataId (correlationID: $correlationId)")

        val qaReviewEntity =
            QaReviewEntity(
                dataId = dataId,
                companyId = dataMetaInfo.companyId,
                companyName = companyName,
                framework = dataMetaInfo.dataType.value,
                reportingPeriod = dataMetaInfo.reportingPeriod,
                timestamp = Instant.now().toEpochMilli(),
                qaStatus = qaStatus,
                triggeringUserId = triggeringUserId,
                comment = comment,
            )
        return qaReviewRepository.save(qaReviewEntity)
    }

    /**
     * Checks if the QA service knows the dataId
     */
    @Transactional
    fun checkIfQaServiceKnowsDataId(dataId: String): Boolean = getMostRecentQaReviewEntity(dataId) != null

    /**
     * Asserts that the QA service knows the dataId
     */
    @Transactional
    fun assertQaServiceKnowsDataId(dataId: String) {
        if (!checkIfQaServiceKnowsDataId(dataId)) {
            throw ResourceNotFoundApiException(
                "Data ID not known to QA service",
                "Dataland does not know the data id $dataId",
            )
        }
    }

    /**
     * Send the information that the QA status was updated to the message queue
     * @param qaReviewEntity qaReviewEntity for which to send the QaStatusChangeMessage
     * @param correlationId the ID for the process triggering the change
     */
    fun sendQaStatusUpdateMessage(
        qaReviewEntity: QaReviewEntity,
        correlationId: String,
    ) {
        val currentlyActiveDataId =
            if (qaReviewEntity.qaStatus == QaStatus.Accepted) {
                qaReviewEntity.dataId
            } else {
                getDataIdOfCurrentlyActiveDataset(
                    qaReviewEntity.companyId,
                    qaReviewEntity.framework,
                    qaReviewEntity.reportingPeriod,
                )
            }

        val qaStatusChangeMessage =
            QaStatusChangeMessage(
                dataId = qaReviewEntity.dataId,
                updatedQaStatus = qaReviewEntity.qaStatus,
                currentlyActiveDataId = currentlyActiveDataId,
            )

        logger.info("Send QA status update message for dataId ${qaStatusChangeMessage.dataId} to messageQueue.")
        val messageBody = objectMapper.writeValueAsString(qaStatusChangeMessage)

        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            messageBody, MessageType.QA_STATUS_UPDATED, correlationId, ExchangeName.QA_SERVICE_DATA_QUALITY_EVENTS,
            RoutingKeyNames.DATA,
        )
    }

    /**
     * Delete all QA Review information from repository
     * @param dataId All QA review information with dataId is deleted
     * @param correlationId correlation Id
     */
    @Transactional
    fun deleteAllByDataId(
        dataId: String,
        correlationId: String,
    ) {
        logger.info("Deleting all QA information associated with dataId $dataId (correlationId: $correlationId)")
        this.qaReviewRepository.deleteAllByDataId(dataId)
    }

    /**
     * Retrieve dataId of currently active dataset for some triple ([companyId], [dataType], [reportingPeriod])
     * @param companyId the ID of the company
     * @param dataType the dataType of the dataset
     * @param reportingPeriod the reportingPeriod of the dataset
     * @return Returns the dataId of the active dataset, or an empty string if no active dataset can be found
     */
    fun getDataIdOfCurrentlyActiveDataset(
        companyId: String,
        dataType: String,
        reportingPeriod: String,
    ): String? {
        logger.info(
            "Searching for currently active dataset for company $companyId, " +
                "dataType $dataType, and reportingPeriod $reportingPeriod",
        )
        val searchFilter =
            QaSearchFilter(
                dataTypes = DataTypeEnum.decode(dataType)?.let { setOf(it) },
                companyIds = setOf(companyId),
                reportingPeriods = setOf(reportingPeriod),
                qaStatuses = setOf(QaStatus.Accepted),
                companyName = null,
            )

        return qaReviewRepository
            .getSortedAndFilteredQaReviewMetadataSet(searchFilter)
            .maxByOrNull { it.timestamp }
            ?.dataId
    }

    /**
     * Calls backend to return companyIds for companyName
     */
    private fun getCompanyIdsForCompanyName(companyName: String?): Set<String> {
        var companyIds = emptySet<String>()
        if (!companyName.isNullOrBlank()) {
            try {
                companyIds =
                    companyDataControllerApi.getCompaniesBySearchString(companyName).map { it.companyId }.toSet()
            } catch (clientException: ClientException) {
                val responseBody = (clientException.response as ClientError<*>).body.toString()
                exceptionForwarder.catchSearchStringTooShortClientException(
                    responseBody,
                    clientException.statusCode,
                    clientException,
                )
                throw clientException
            }
        }
        return companyIds
    }
}
