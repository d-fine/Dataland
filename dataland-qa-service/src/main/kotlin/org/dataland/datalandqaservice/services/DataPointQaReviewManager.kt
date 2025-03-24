package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandmessagequeueutils.messages.data.CopyQaStatusFromDataset
import org.dataland.datalandmessagequeueutils.messages.data.DataPointUploadedPayload
import org.dataland.datalandmessagequeueutils.messages.data.PresetQaStatus
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataPointQaReviewInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointQaReviewRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.DataPointQaReviewItemFilter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * A service class for managing QA related information for data points
 */
@Service
class DataPointQaReviewManager
    @Autowired
    @Suppress("LongParameterList")
    constructor(
        private val dataPointQaReviewRepository: DataPointQaReviewRepository,
        private val cloudEventMessageHandler: CloudEventMessageHandler,
        private val objectMapper: ObjectMapper,
        private val compositionService: DataPointCompositionService,
        private val qaReviewManager: QaReviewManager,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Review a data point and change its QA status
         * @param dataPointId dataId of dataset of which to change qaStatus
         * @param qaStatus new qaStatus to be set
         * @param triggeringUserId keycloakId of user triggering QA Status change or upload event
         * @param correlationId the ID for the process triggering the change
         */
        data class ReviewDataPointTask(
            val dataPointId: String,
            val qaStatus: QaStatus,
            val triggeringUserId: String,
            val comment: String?,
            val correlationId: String,
            val timestamp: Long,
        )

        /**
         * Review a list of data points and change their QA status
         */
        @Transactional
        fun reviewDataPoints(tasks: List<ReviewDataPointTask>): List<DataPointQaReviewEntity> {
            val reviewEntities = createDataPointReviewEntities(tasks)
            sendBulkDataPointQaStatusChangeMessages(reviewEntities)
            return reviewEntities.map { it.first }
        }

        /**
         * All data required for the reviewDataPointFromMessages function (i.e., the message and the correlationId)
         */
        data class DataPointUploadedMessageWithCorrelationId(
            val message: DataPointUploadedPayload,
            val correlationId: String,
        )

        private fun createQaEntryFromPresetInitialQa(
            message: DataPointUploadedPayload,
            initialQaStatus: PresetQaStatus,
        ): DataPointQaReviewEntity =
            DataPointQaReviewEntity(
                dataPointId = message.dataPointId,
                companyId = message.companyId,
                companyName = message.companyName,
                dataPointType = message.dataPointType,
                reportingPeriod = message.reportingPeriod,
                timestamp = message.uploadTime,
                qaStatus = initialQaStatus.qaStatus,
                triggeringUserId = message.uploaderUserId,
                comment = initialQaStatus.qaComment,
            )

        private fun createQaEntryFromCopyInitialQa(
            message: DataPointUploadedPayload,
            initialQaStatus: CopyQaStatusFromDataset,
            correlationId: String,
        ): DataPointQaReviewEntity {
            val dataPointQaReviewEntity = qaReviewManager.getMostRecentQaReviewEntity(initialQaStatus.datasetId)
            if (dataPointQaReviewEntity == null) {
                logger.warn(
                    "Could not find QA review entity for dataset ${initialQaStatus.datasetId} " +
                        "- Setting DataPoint status to Pending (correlationID: $correlationId)",
                )
                return createQaEntryFromPresetInitialQa(
                    message,
                    PresetQaStatus(QaStatus.Pending, null),
                )
            }

            return DataPointQaReviewEntity(
                dataPointId = message.dataPointId,
                companyId = message.companyId,
                companyName = message.companyName,
                dataPointType = message.dataPointType,
                reportingPeriod = message.reportingPeriod,
                timestamp = dataPointQaReviewEntity.timestamp,
                qaStatus = dataPointQaReviewEntity.qaStatus,
                triggeringUserId = dataPointQaReviewEntity.triggeringUserId,
                comment = dataPointQaReviewEntity.comment,
            )
        }

        /**
         * Review a data point and change its QA status using the information provided in the datapoint uploaded message
         * @param messages the messages containing the information to review
         */
        @Transactional
        fun reviewDataPointFromMessages(messages: List<DataPointUploadedMessageWithCorrelationId>): List<DataPointQaReviewEntity> {
            val reviewEntities = mutableListOf<Pair<DataPointQaReviewEntity, String>>()
            for (messageWithCorrId in messages) {
                val message = messageWithCorrId.message
                val correlationId = messageWithCorrId.correlationId
                val dataPointQaReviewEntity =
                    when (val initialQa = message.initialQa) {
                        is PresetQaStatus -> createQaEntryFromPresetInitialQa(message, initialQa)
                        is CopyQaStatusFromDataset -> createQaEntryFromCopyInitialQa(message, initialQa, correlationId)
                    }
                logger.info(
                    "Assigning quality status ${dataPointQaReviewEntity.qaStatus} to data point with ID " +
                        "${message.dataPointId} (correlationID: $correlationId)",
                )
                reviewEntities.add(Pair(dataPointQaReviewEntity, correlationId))
            }

            val savedEntities = dataPointQaReviewRepository.saveAll(reviewEntities.map { it.first })

            sendBulkDataPointQaStatusChangeMessages(reviewEntities)
            return savedEntities
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
            val timestamp = Instant.now().toEpochMilli()
            val allQaTasks =
                allDataIds.map {
                    ReviewDataPointTask(
                        dataPointId = it,
                        qaStatus = qaStatus,
                        triggeringUserId = triggeringUserId,
                        comment = comment,
                        correlationId = correlationId,
                        timestamp = timestamp,
                    )
                }
            val filteredTasks =
                if (overwriteDataPointQaStatus) {
                    allQaTasks
                } else {
                    val qaStatusOfAllDataIds =
                        dataPointQaReviewRepository
                            .findLatestWhereDataPointIdIn(allDataIds)
                            .associate { it.dataPointId to it.qaStatus }
                    allQaTasks.filter {
                        it.dataPointId !in qaStatusOfAllDataIds ||
                            qaStatusOfAllDataIds[it.dataPointId] == QaStatus.Pending
                    }
                }
            reviewDataPoints(filteredTasks)
        }

        /**
         * Asserts that the QA service knows the dataId
         */
        @Transactional(readOnly = true)
        fun assertQaServiceKnowsDataPointId(dataPointId: String) {
            if (dataPointQaReviewRepository.findFirstByDataPointIdOrderByTimestampDesc(dataPointId) == null) {
                throw ResourceNotFoundApiException(
                    "Data Point ID not known to QA service",
                    "Dataland does not know the data point with id $dataPointId",
                )
            }
        }

        private fun createDataPointReviewEntities(tasks: List<ReviewDataPointTask>): List<Pair<DataPointQaReviewEntity, String>> {
            val anyExistingReviewEntity =
                dataPointQaReviewRepository
                    .findAllByDataPointIdIn(tasks.map { it.dataPointId })
                    .associateBy { it.dataPointId }
            val createdEntries = mutableListOf<Pair<DataPointQaReviewEntity, String>>()
            for (task in tasks) {
                val existingEntry =
                    anyExistingReviewEntity[task.dataPointId]
                        ?: throw ResourceNotFoundApiException(
                            "Data Point not found in QA database.",
                            "The data point with ID ${task.dataPointId} was not yet found in the QA database. " +
                                "Please retry in a few seconds.",
                        )
                logger.info(
                    "Assigning quality status ${task.qaStatus} to data point with ID ${task.dataPointId}" +
                        " (correlationID: ${task.correlationId})",
                )

                val dataPointQaReviewEntity =
                    DataPointQaReviewEntity(
                        dataPointId = task.dataPointId,
                        companyId = existingEntry.companyId,
                        companyName = existingEntry.companyName,
                        dataPointType = existingEntry.dataPointType,
                        reportingPeriod = existingEntry.reportingPeriod,
                        timestamp = task.timestamp,
                        qaStatus = task.qaStatus,
                        triggeringUserId = task.triggeringUserId,
                        comment = task.comment,
                    )
                createdEntries.add(Pair(dataPointQaReviewEntity, task.correlationId))
            }
            dataPointQaReviewRepository.saveAll(createdEntries.map { it.first })
            return createdEntries
        }

        private fun sendBulkDataPointQaStatusChangeMessages(
            reviewEntitiesWithCorrelationIds: List<Pair<DataPointQaReviewEntity, String>>,
        ) {
            val allCompanyIds = reviewEntitiesWithCorrelationIds.map { it.first.companyId }.distinct()
            val allDataPointTypes = reviewEntitiesWithCorrelationIds.map { it.first.dataPointType }.distinct()
            val allReportingPeriods = reviewEntitiesWithCorrelationIds.map { it.first.reportingPeriod }.distinct()

            val activeDataPoints =
                dataPointQaReviewRepository
                    .getActiveDataPointsForAllTriplets(allCompanyIds, allDataPointTypes, allReportingPeriods)
                    .associate { Triple(it.companyId, it.dataPointType, it.reportingPeriod) to it.dataPointId }

            reviewEntitiesWithCorrelationIds.forEach { (reviewEntity, correlationId) ->
                val qaStatusChangeMessage =
                    QaStatusChangeMessage(
                        dataId = reviewEntity.dataPointId,
                        updatedQaStatus = reviewEntity.qaStatus,
                        currentlyActiveDataId =
                            activeDataPoints[
                                Triple(
                                    reviewEntity.companyId,
                                    reviewEntity.dataPointType,
                                    reviewEntity.reportingPeriod,
                                ),
                            ],
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
