package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandmessagequeueutils.messages.data.DataPointUploadedPayload
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
class DataPointQaReviewManager
    @Autowired
    constructor(
        private val dataPointQaReviewRepository: DataPointQaReviewRepository,
        private val companyDataControllerApi: CompanyDataControllerApi,
        private val dataPointControllerApi: DataPointControllerApi,
        private val cloudEventMessageHandler: CloudEventMessageHandler,
        private val objectMapper: ObjectMapper,
        private val compositionService: DataPointCompositionService,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Review a data point and change its QA status
         * @param dataPointId dataId of dataset of which to change qaStatus
         * @param qaStatus new qaStatus to be set
         * @param triggeringUserId keycloakId of user triggering QA Status change or upload event
         * @param correlationId the ID for the process triggering the change
         */
        @Transactional
        fun reviewDataPoint(
            dataPointId: String,
            qaStatus: QaStatus,
            triggeringUserId: String,
            comment: String?,
            correlationId: String,
        ): DataPointQaReviewEntity {
            val reviewEntity = saveDataPointQaReviewEntity(dataPointId, qaStatus, triggeringUserId, comment, correlationId)
            sendDataPointQaStatusChangeMessage(reviewEntity, correlationId)
            return reviewEntity
        }

        /**
         * All data required for the reviewDataPointFromMessages function (i.e., the message and the correlationId)
         */
        data class DataPointUploadedMessageWithCorrelationId(
            val message: DataPointUploadedPayload,
            val correlationId: String,
        )

        /**
         * Review a data point and change its QA status using the information provided in the datapoint uploaded message
         * @param messages the messages containing the information to review
         */
        @Transactional
        fun reviewDataPointFromMessages(messages: List<DataPointUploadedMessageWithCorrelationId>): List<DataPointQaReviewEntity> {
            val reviewEntities = mutableListOf<DataPointQaReviewEntity>()
            for (messageWithCorrId in messages) {
                val message = messageWithCorrId.message
                val correlationId = messageWithCorrId.correlationId
                logger.info(
                    "Assigning quality status ${message.initialQaStatus} to data point with ID " +
                        "${message.dataPointId} (correlationID: $correlationId)",
                )

                val dataPointQaReviewEntity =
                    DataPointQaReviewEntity(
                        dataPointId = message.dataPointId,
                        companyId = message.companyId,
                        companyName = message.companyName,
                        dataPointType = message.dataPointType,
                        reportingPeriod = message.reportingPeriod,
                        timestamp = message.uploadTime,
                        qaStatus = QaStatus.valueOf(message.initialQaStatus),
                        triggeringUserId = message.uploaderUserId,
                        comment = message.initialQaComment,
                    )
                reviewEntities.add(dataPointQaReviewEntity)
                sendDataPointQaStatusChangeMessage(dataPointQaReviewEntity, correlationId)
            }

            return dataPointQaReviewRepository.saveAll(reviewEntities)
        }

        /**
         * Review an assembled dataset and change the QA status of the contained data points depending on [overwriteDataPointQaStatus]
         * @param dataId dataId of dataset of which to change qaStatus
         * @param qaStatus new qaStatus to be set
         * @param triggeringUserId keycloakId of user triggering QA Status change or upload event
         * @param correlationId the ID for the process triggering the change
         * @param overwriteDataPointQaStatus if true: the QA status of all data points in the dataset will be overwritten,
         * if false: only data points with QA status 'Pending' will be updated
         */
        @Transactional
        fun reviewAssembledDataset(
            dataId: String,
            qaStatus: QaStatus,
            triggeringUserId: String,
            comment: String?,
            correlationId: String,
            overwriteDataPointQaStatus: Boolean,
        ) {
            val composition = compositionService.getCompositionOfDataset(dataId) ?: return
            val allDataIds = composition.values.toList()
            allDataIds.forEach {
                assertQaServiceKnowsDataPointId(it)
            }

            if (overwriteDataPointQaStatus) {
                allDataIds.forEach {
                    reviewDataPoint(it, qaStatus, triggeringUserId, comment, correlationId)
                }
            } else {
                val qaStatusOfAllDataIds =
                    dataPointQaReviewRepository
                        .findLatestWhereDataPointIdIn(allDataIds)
                        .associate { it.dataPointId to it.qaStatus }
                allDataIds.forEach {
                    if (it !in qaStatusOfAllDataIds || qaStatusOfAllDataIds[it] == QaStatus.Pending) {
                        reviewDataPoint(it, qaStatus, triggeringUserId, comment, correlationId)
                    }
                }
            }
        }

        /**
         * Asserts that the QA service knows the dataId
         */
        @Transactional
        fun assertQaServiceKnowsDataPointId(dataPointId: String) {
            if (!checkIfQaServiceKnowsDataPointId(dataPointId)) {
                throw ResourceNotFoundApiException(
                    "Data Point ID not known to QA service",
                    "Dataland does not know the data point with id $dataPointId",
                )
            }
        }

        /**
         * Checks if the QA service knows the dataId
         */
        @Transactional
        fun checkIfQaServiceKnowsDataPointId(dataPointId: String): Boolean =
            dataPointQaReviewRepository.findFirstByDataPointIdOrderByTimestampDesc(dataPointId) != null

        private fun saveDataPointQaReviewEntity(
            dataPointId: String,
            qaStatus: QaStatus,
            triggeringUserId: String,
            comment: String?,
            correlationId: String,
        ): DataPointQaReviewEntity {
            val dataMetaInfo = dataPointControllerApi.getDataPointMetaInfo(dataPointId)
            val companyName = companyDataControllerApi.getCompanyById(dataMetaInfo.companyId).companyInformation.companyName

            logger.info("Assigning quality status $qaStatus to data point with ID $dataPointId (correlationID: $correlationId)")

            val dataPointQaReviewEntity =
                DataPointQaReviewEntity(
                    dataPointId = dataPointId,
                    companyId = dataMetaInfo.companyId,
                    companyName = companyName,
                    dataPointType = dataMetaInfo.dataPointType,
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
                    dataPointQaReviewEntity.dataPointId
                } else {
                    getDataIdOfCurrentlyActiveDataPoint(
                        dataPointQaReviewEntity.companyId,
                        dataPointQaReviewEntity.dataPointType,
                        dataPointQaReviewEntity.reportingPeriod,
                    )
                }

            val qaStatusChangeMessage =
                QaStatusChangeMessage(
                    dataId = dataPointQaReviewEntity.dataPointId,
                    updatedQaStatus = dataPointQaReviewEntity.qaStatus,
                    currentlyActiveDataId = currentlyActiveDataId,
                )

            logger.info("Publishing QA status change message for dataId ${qaStatusChangeMessage.dataId}.")
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                body = objectMapper.writeValueAsString(qaStatusChangeMessage),
                type = MessageType.QA_STATUS_UPDATED,
                correlationId = correlationId,
                exchange = ExchangeName.QA_SERVICE_DATA_QUALITY_EVENTS,
                routingKey = RoutingKeyNames.DATA_POINT_QA,
            )
        }

        /**
         * Retrieve dataId of currently active dataset for same triple (companyId, dataType, reportingPeriod)
         * @param companyId ID of the company the data point is associated to
         * @param dataPointType Identifier of the type of the data point
         * @param reportingPeriod Reporting period of the data point
         * @return Returns the dataId of the active data point, or null if no active dataset can be found
         */
        private fun getDataIdOfCurrentlyActiveDataPoint(
            companyId: String,
            dataPointType: String,
            reportingPeriod: String,
        ): String? {
            logger.info(
                "Searching for currently active data point for company $companyId, " +
                    "data point identifier $dataPointType, and reportingPeriod $reportingPeriod",
            )
            val searchFilter =
                BasicDataPointDimensions(
                    companyId = companyId,
                    dataPointType = dataPointType,
                    reportingPeriod = reportingPeriod,
                )

            return dataPointQaReviewRepository.getDataPointIdOfCurrentlyActiveDataPoint(searchFilter)
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
                .findByDataPointIdOrderByTimestampDesc(dataId)
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
