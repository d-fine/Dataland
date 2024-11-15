package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.ExceptionForwarder
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.QaReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.toDatasetQaReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.QaSearchFilter
import org.dataland.datalandqaservice.repositories.QaReviewRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
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
     * The method returns a list of unreviewed datasets with corresponding information for the specified input params
     * @param dataTypes the datatype of the dataset
     * @param reportingPeriods the reportingPeriod of the dataset
     * @param companyName the company name connected to the dataset
     * @param chunkIndex the chunkIndex of the request
     * @param chunkSize the chunkSize of the request
     */
    fun getInfoOnPendingDatasets(
        dataTypes: Set<DataTypeEnum>?,
        reportingPeriods: Set<String>?,
        companyName: String?,
        chunkSize: Int,
        chunkIndex: Int,
        userIsAdmin: Boolean,
    ): List<QaReviewResponse> {
        val offset = (chunkIndex) * (chunkSize)
        return qaReviewRepository
            .getSortedAndFilteredQaReviewMetadataSet(
                QaSearchFilter(
                    dataTypes = dataTypes,
                    reportingPeriods = reportingPeriods,
                    companyIds = getCompanyIdsForCompanyName(companyName),
                    companyName = companyName,
                    qaStatuses = setOf(QaStatus.Pending),
                ),
                resultOffset = offset,
                resultLimit = chunkSize,
            ).map { it.toDatasetQaReviewResponse(userIsAdmin) }
    }

    /**
     * This method returns the number of unreviewed datasets for a specific set of filters
     * @param dataTypes the set of datatypes for which should be filtered
     * @param reportingPeriods the set of reportingPeriods for which should be filtered
     * @param companyName the companyName for which should be filtered
     */
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
     * Retrieves from database a QaReviewEntity by its dataId
     * @param dataId: dataID
     */
    fun getQaReviewResponseByDataId(
        dataId: UUID,
        userIsAdmin: Boolean,
    ): QaReviewResponse? =
        qaReviewRepository
            .findFirstByDataIdOrderByTimestampDesc(dataId.toString())
            ?.toDatasetQaReviewResponse(userIsAdmin)

    /**
     * Saves QaReviewEntity to database and sends status change message to MessageQueue
     * @param dataId dataId of dataset of which to change qaStatus
     * @param qaStatus new qaStatus to be set
     * @param reviewerId keycloakId of reviewer
     * @param correlationId
     */
    fun saveQaReviewEntityAndSendQaStatusChangeMessage(
        dataId: String,
        qaStatus: QaStatus,
        reviewerId: String,
        comment: String?,
        correlationId: String,
    ) {
        val dataMetaInfo = metaDataControllerApi.getDataMetaInfo(dataId)
        val companyName = companyDataControllerApi.getCompanyById(dataMetaInfo.companyId).companyInformation.companyName

        logger.info("Assigning quality status ${qaStatus.name} to dataset with ID $dataId")

        val qaReviewEntity =
            QaReviewEntity(
                dataId = dataId,
                companyId = dataMetaInfo.companyId,
                companyName = companyName,
                dataType = dataMetaInfo.dataType,
                reportingPeriod = dataMetaInfo.reportingPeriod,
                timestamp = Instant.now().toEpochMilli(),
                qaStatus = qaStatus,
                reviewerId = reviewerId,
                comment = comment,
            )

        qaReviewRepository.save(qaReviewEntity)

        val qaStatusChangeMessage =
            QaStatusChangeMessage(
                changedQaStatusDataId = qaReviewEntity.dataId,
                updatedQaStatus = qaReviewEntity.qaStatus,
                currentlyActiveDataId =
                    getDataIdOfCurrentlyActiveDataset(
                        qaReviewEntity.companyId,
                        qaReviewEntity.dataType,
                        qaReviewEntity.reportingPeriod,
                    ),
            )

        sendQaStatusChangeMessage(
            qaStatusChangeMessage = qaStatusChangeMessage,
            correlationId = correlationId,
        )
    }

    /**
     * Sends the QA Status Change Message to MessageQueue
     * @param qaStatusChangeMessage QAStatusChangeMessage containing the dataId of the changed data set, the new QA
     * status and the dataId of the newly active dataset
     * @param correlationId the ID of the process
     */
    private fun sendQaStatusChangeMessage(
        qaStatusChangeMessage: QaStatusChangeMessage,
        correlationId: String,
    ) {
        logger.info("Send QA status change message to messageQueue.")
        val messageBody = objectMapper.writeValueAsString(qaStatusChangeMessage)

        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            messageBody, MessageType.QA_STATUS_CHANGED, correlationId, ExchangeName.DATA_QUALITY_ASSURED,
            RoutingKeyNames.DATA,
        )
    }

    /**
     * Retrieve dataId of currently active dataset for same triple (companyId, dataType, reportingPeriod)
     * @param companyId
     * @param dataType
     * @param reportingPeriod
     * @return Returns the dataId of the active dataset, or an empty string if no active dataset can be found
     */
    private fun getDataIdOfCurrentlyActiveDataset(
        companyId: String,
        dataType: DataTypeEnum,
        reportingPeriod: String,
    ): String =
        qaReviewRepository
            .findByCompanyIdAndDataTypeAndReportingPeriod(companyId, dataType, reportingPeriod)
            ?.filter { it.qaStatus == QaStatus.Accepted }
            ?.maxByOrNull { it.timestamp }
            ?.dataId ?: ""

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
