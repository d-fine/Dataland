package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataPointQaReviewInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointQaReviewRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.DataPointQaReviewItemFilter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * A service class for managing QA related information for data points
 */
@Service
class DataPointQaReviewManager(
    @Autowired val dataPointQaReviewRepository: DataPointQaReviewRepository,
    @Autowired val companyDataControllerApi: CompanyDataControllerApi,
    @Autowired val dataPointControllerApi: DataPointControllerApi,
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Review a data point and change its QA status
     * @param dataId dataId of dataset of which to change qaStatus
     * @param qaStatus new qaStatus to be set
     * @param triggeringUserId keycloakId of user triggering QA Status change or upload event
     * @param correlationId the ID for the process triggering the change
     */
    @Transactional
    fun reviewDataPoint(
        dataId: String,
        qaStatus: QaStatus,
        triggeringUserId: String,
        comment: String?,
        correlationId: String,
    ): DataPointQaReviewEntity {
        val reviewEntity = saveDataPointQaReviewEntity(dataId, qaStatus, triggeringUserId, comment, correlationId)
        sendDataPointQaStatusChangeMessage(reviewEntity, correlationId)
        return reviewEntity
    }

    private fun saveDataPointQaReviewEntity(
        dataId: String,
        qaStatus: QaStatus,
        triggeringUserId: String,
        comment: String?,
        correlationId: String,
    ): DataPointQaReviewEntity {
        val dataMetaInfo = dataPointControllerApi.getDataPointMetaInfo(dataId)
        val companyName = companyDataControllerApi.getCompanyById(dataMetaInfo.companyId).companyInformation.companyName

        logger.info("Assigning quality status $qaStatus to data point with ID $dataId (correlationID: $correlationId)")

        val dataPointQaReviewEntity =
            DataPointQaReviewEntity(
                dataId = dataId,
                companyId = dataMetaInfo.companyId,
                companyName = companyName,
                dataPointIdentifier = dataMetaInfo.dataPointIdentifier,
                reportingPeriod = dataMetaInfo.reportingPeriod,
                timestamp = Instant.now().toEpochMilli(),
                qaStatus = qaStatus,
                triggeringUserId = triggeringUserId,
                comment = comment,
            )
        return dataPointQaReviewRepository.save(dataPointQaReviewEntity)
    }

    private fun sendDataPointQaStatusChangeMessage(
        dataPointQaReviewEntity: DataPointQaReviewEntity,
        correlationId: String,
    ) {
        val currentlyActiveDataId =
            if (dataPointQaReviewEntity.qaStatus == QaStatus.Accepted) {
                dataPointQaReviewEntity.dataId
            } else {
                getDataIdOfCurrentlyActiveDataPoint(
                    dataPointQaReviewEntity.companyId,
                    dataPointQaReviewEntity.dataPointIdentifier,
                    dataPointQaReviewEntity.reportingPeriod,
                )
            }

        val qaStatusChangeMessage =
            QaStatusChangeMessage(
                dataId = dataPointQaReviewEntity.dataId,
                updatedQaStatus = dataPointQaReviewEntity.qaStatus,
                currentlyActiveDataId = currentlyActiveDataId,
            )

        logger.info("Publishing QA status change message for dataId ${qaStatusChangeMessage.dataId}.")
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body = objectMapper.writeValueAsString(qaStatusChangeMessage),
            type = MessageType.QA_STATUS_CHANGED,
            correlationId = correlationId,
            exchange = ExchangeName.QA_SERVICE_DATA_QUALITY_EVENTS,
            routingKey = RoutingKeyNames.DATA_POINT_QA,
        )
    }

    /**
     * Retrieve dataId of currently active dataset for same triple (companyId, dataType, reportingPeriod)
     * @param companyId ID of the company the data point is associated to
     * @param dataPointIdentifier Identifier of the type of the data point
     * @param reportingPeriod Reporting period of the data point
     * @return Returns the dataId of the active data point, or null if no active dataset can be found
     */
    private fun getDataIdOfCurrentlyActiveDataPoint(
        companyId: String,
        dataPointIdentifier: String,
        reportingPeriod: String,
    ): String? {
        logger.info(
            "Searching for currently active data point for company $companyId, " +
                "data point identifier $dataPointIdentifier, and reportingPeriod $reportingPeriod",
        )
        val searchFilter =
            DataPointQaReviewItemFilter(
                companyId = companyId,
                dataPointIdentifier = dataPointIdentifier,
                reportingPeriod = reportingPeriod,
                qaStatus = QaStatus.Accepted.toString(),
            )

        return dataPointQaReviewRepository.getDataIdOfCurrentlyActiveDataPoint(searchFilter)
    }

    /**
     * Retrieve all data points currently in the QA review queue (i.e. with status 'Pending')
     */
    fun getDataPointQaReviewQueue(): List<DataPointQaReviewInformation> =
        dataPointQaReviewRepository
            .getAllEntriesForTheReviewQueue()
            .map { it.toDataPointQaReviewInformation() }

    /**
     * Retrieve all the QA review information for a specific data point ID ordered by descending timestamp
     */
    fun getDataPointQaReviewInformationByDataId(dataId: String): List<DataPointQaReviewInformation> =
        dataPointQaReviewRepository
            .findByDataIdOrderByTimestampDesc(dataId)
            .map { it.toDataPointQaReviewInformation() }

    /**
     * Retrieve all QA review information items matching the provided filters in descending order by timestamp.
     * Results are paginated using [chunkSize] and [chunkIndex].
     * @param searchFilter the filter to apply containing the company ID, data point identifier, reporting period and the QA status
     * @param onlyLatest if true, only the latest entry for each dataId is returned
     * @param chunkSize the number of results to return
     * @param chunkIndex the index to start the result set from
     *
     */
    fun getFilteredDataPointQaReviewInformation(
        searchFilter: DataPointQaReviewItemFilter,
        onlyLatest: Boolean? = true,
        chunkSize: Int? = 10,
        chunkIndex: Int? = 0,
    ): List<DataPointQaReviewInformation> =
        if (onlyLatest == true) {
            dataPointQaReviewRepository
                .findByFilterLatestOnly(searchFilter, chunkSize, chunkIndex)
                .map { it.toDataPointQaReviewInformation() }
        } else {
            dataPointQaReviewRepository
                .findByFilter(searchFilter, chunkSize, chunkIndex)
                .map { it.toDataPointQaReviewInformation() }
        }
}
