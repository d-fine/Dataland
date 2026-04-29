package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.QaBypass
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReviewEntity
import org.dataland.datalandqaservice.repositories.QaReviewRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID.randomUUID

/**
 * Command/orchestration service for dataset-level QA review changes.
 */
@Suppress("LongParameterList")
@Service
class QaReviewManager
    @Autowired
    constructor(
        val qaReviewRepository: QaReviewRepository,
        val companyDataControllerApi: CompanyDataControllerApi,
        val metaDataControllerApi: MetaDataControllerApi,
        var cloudEventMessageHandler: CloudEventMessageHandler,
        var objectMapper: ObjectMapper,
        val dataPointQaReviewManager: DataPointQaReviewManager,
        val qaReviewQueryService: QaReviewQueryService,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Add a new qa review entry corresponding to a dataset event (upload, qa status change, etc.) to the qa review
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

            handleQaChange(
                dataId = dataId,
                qaStatus = qaStatus,
                triggeringUserId = triggeringUserId,
                comment = comment,
                correlationId = correlationId,
            )
        }

        /**
         * Patches the uploaderUserId in the first QA review entry for a dataset.
         *
         * @param dataId identifier of the dataset
         * @param uploaderUserId new uploader user ID to set
         * @param correlationId the ID for the process triggering the change
         */
        @Transactional
        fun patchUploaderUserIdInQaReviewEntry(
            dataId: String,
            uploaderUserId: String,
            correlationId: String,
        ) {
            logger.info("Received message to patch uploaderUserId for dataset with dataId $dataId (correlationId: $correlationId).")
            val qaReviewEntity = qaReviewRepository.findFirstByDataIdOrderByTimestampAsc(dataId)

            requireNotNull(qaReviewEntity) { "QaReviewEntity must not be null." }

            logger.info(
                "Updating triggeringUserId for first qa review entry for dataset with dataId $dataId$. " +
                    "Old triggeringUserId was ${qaReviewEntity.triggeringUserId}, new triggeringUserId is $uploaderUserId " +
                    "(correlationId: $correlationId).",
            )

            qaReviewEntity.triggeringUserId = uploaderUserId
        }

        /**
         * Handles Qa changes by creating and saving QaReviewEntity, and sending messages.
         * @param dataId dataId of dataset of which to change qaStatus
         * @param qaStatus new qaStatus to be set
         * @param triggeringUserId keycloakId of user triggering QA Status change or upload event
         * @param correlationId the ID for the process triggering the change
         */
        @Transactional
        fun handleQaChange(
            dataId: String,
            qaStatus: QaStatus,
            triggeringUserId: String,
            comment: String?,
            correlationId: String,
        ) {
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
            qaReviewRepository.save(qaReviewEntity)
            qaReviewQueryService
                .getAcceptedReviewMetadataSorted(
                    qaReviewEntity.companyId,
                    qaReviewEntity.framework,
                    qaReviewEntity.reportingPeriod,
                ).also {
                    this.sendQaStatusUpdateMessage(
                        qaReviewEntity = qaReviewEntity,
                        correlationId = correlationId,
                        isUpdate = it.size > 1,
                        newActiveDataId = it.firstOrNull()?.dataId,
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
            isUpdate: Boolean,
            newActiveDataId: String?,
        ) {
            val qaStatusChangeMessage =
                QaStatusChangeMessage(
                    dataId = qaReviewEntity.dataId,
                    updatedQaStatus = qaReviewEntity.qaStatus,
                    currentlyActiveDataId = newActiveDataId,
                    basicDataDimensions =
                        BasicDataDimensions(
                            companyId = qaReviewEntity.companyId,
                            dataType = qaReviewEntity.framework,
                            reportingPeriod = qaReviewEntity.reportingPeriod,
                        ),
                    isUpdate = isUpdate,
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
         * Changes the QA status of a dataset.
         *
         * Creates a new correlation ID, records the dataset-level QA status change triggered by the current reviewer,
         * and propagates the same review decision to the assembled dataset's data points. Data point statuses are only
         * overwritten when `overwriteDataPointQaStatus` is set to `true`.
         *
         * @param dataId identifier of the dataset whose QA status is changed
         * @param qaStatus new QA status to assign
         * @param comment optional reviewer comment explaining the status change
         * @param overwriteDataPointQaStatus whether existing data point QA statuses should be overwritten
         * @return the generated correlation ID used to trace the status change workflow
         */
        @Transactional
        fun changeQaStatus(
            dataId: String,
            qaStatus: QaStatus,
            comment: String?,
            overwriteDataPointQaStatus: Boolean,
        ): String {
            val correlationId = randomUUID().toString()
            val reviewerId = DatalandAuthentication.fromContext().userId
            logger.info(
                "User $reviewerId requested QA status change of dataset $dataId to $qaStatus (correlationId: $correlationId)",
            )

            handleQaChange(
                dataId = dataId,
                qaStatus = qaStatus,
                triggeringUserId = reviewerId,
                comment = comment,
                correlationId = correlationId,
            )

            dataPointQaReviewManager.reviewAssembledDataset(
                dataId = dataId,
                qaStatus = qaStatus,
                triggeringUserId = reviewerId,
                comment = comment,
                correlationId = correlationId,
                overwriteDataPointQaStatus = overwriteDataPointQaStatus,
            )

            return correlationId
        }
    }
