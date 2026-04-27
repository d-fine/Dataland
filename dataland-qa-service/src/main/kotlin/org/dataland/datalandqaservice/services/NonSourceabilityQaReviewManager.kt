package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
import org.dataland.datalandqaservice.entities.NonSourceableQaReviewInformationEntity
import org.dataland.datalandqaservice.model.NonSourceableQaReviewInformation
import org.dataland.datalandqaservice.repositories.NonSourceableQaReviewRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
 * Manages QA review decisions for non-sourceability entries.
 *
 * Accepts or rejects a pending non-sourceability review and emits the corresponding
 * lifecycle event consumed by backend and data-sourcing-service.
 */
@Service
class NonSourceabilityQaReviewManager
    @Autowired
    constructor(
        private val repository: NonSourceableQaReviewRepository,
        private val cloudEventMessageHandler: CloudEventMessageHandler,
        private val objectMapper: ObjectMapper,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Returns all QA review records matching the given optional filters.
         */
        @Transactional(readOnly = true)
        fun getReviews(
            companyId: String?,
            dataType: String?,
            reportingPeriod: String?,
            qaStatus: QaStatus?,
            chunkSize: Int,
            chunkIndex: Int,
        ): List<NonSourceableQaReviewInformation> =
            repository
                .findByQaStatusFilter(qaStatus)
                .filter { entity ->
                    val companyMatches = companyId == null || entity.companyId == companyId
                    val dataTypeMatches = dataType == null || entity.dataType == dataType
                    val reportingPeriodMatches = reportingPeriod == null || entity.reportingPeriod == reportingPeriod
                    companyMatches && dataTypeMatches && reportingPeriodMatches
                }.drop(chunkIndex * chunkSize)
                .take(chunkSize)
                .map { it.toResponse() }

        /**
         * Returns all pending QA review records.
         */
        @Transactional(readOnly = true)
        fun getQueue(): List<NonSourceableQaReviewInformation> = repository.findByQaStatusFilter(QaStatus.Pending).map { it.toResponse() }

        /**
         * Applies a QA decision ([QaStatus.Accepted] or [QaStatus.Rejected]) to the given [nonSourceabilityId].
         *
         * Emits a [NonSourceabilityLifecycleEvent] to [ExchangeName.QA_SERVICE_NON_SOURCEABILITY_DECISIONS]
         * so that backend and data-sourcing-service can apply the transition.
         *
         * @throws ResourceNotFoundApiException if no pending review record exists for [nonSourceabilityId].
         * @throws IllegalArgumentException if [qaStatus] is not Accepted or Rejected.
         */
        @Transactional
        fun postDecision(
            nonSourceabilityId: String,
            qaStatus: QaStatus,
            qaComment: String?,
            reviewerUserId: String,
        ): NonSourceableQaReviewInformation {
            require(qaStatus == QaStatus.Accepted || qaStatus == QaStatus.Rejected) {
                "QA decision must be Accepted or Rejected, got $qaStatus"
            }
            val entity = updateReviewEntity(nonSourceabilityId, qaStatus, qaComment, reviewerUserId)
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(
                    object : TransactionSynchronization {
                        override fun afterCommit() {
                            sendQaDecisionEvent(entity, qaStatus, nonSourceabilityId)
                        }
                    },
                )
            } else {
                sendQaDecisionEvent(entity, qaStatus, nonSourceabilityId)
            }
            return entity.toResponse()
        }

        private fun updateReviewEntity(
            nonSourceabilityId: String,
            qaStatus: QaStatus,
            qaComment: String?,
            reviewerUserId: String,
        ): NonSourceableQaReviewInformationEntity {
            val entity =
                repository.findByNonSourceabilityId(nonSourceabilityId)
                    ?: throw ResourceNotFoundApiException(
                        "Non-sourceability review not found",
                        "No QA review record exists for nonSourceabilityId=$nonSourceabilityId",
                    )
            entity.qaStatus = qaStatus
            entity.reviewerUserId = reviewerUserId
            entity.qaComment = qaComment
            return repository.save(entity)
        }

        private fun sendQaDecisionEvent(
            entity: NonSourceableQaReviewInformationEntity,
            qaStatus: QaStatus,
            nonSourceabilityId: String,
        ) {
            val event =
                NonSourceabilityLifecycleEvent(
                    nonSourceabilityId = nonSourceabilityId,
                    companyId = entity.companyId,
                    dataType = entity.dataType,
                    reportingPeriod = entity.reportingPeriod,
                )
            val messageType =
                if (qaStatus == QaStatus.Accepted) {
                    MessageType.NON_SOURCEABILITY_QA_ACCEPTED
                } else {
                    MessageType.NON_SOURCEABILITY_QA_REJECTED
                }
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                objectMapper.writeValueAsString(event),
                messageType,
                entity.nonSourceabilityId,
                ExchangeName.QA_SERVICE_NON_SOURCEABILITY_DECISIONS,
                RoutingKeyNames.NON_SOURCEABILITY_QA_DECISION,
            )
            logger.info(
                "Emitted $messageType for nonSourceabilityId=$nonSourceabilityId",
            )
        }

        private fun NonSourceableQaReviewInformationEntity.toResponse() =
            NonSourceableQaReviewInformation(
                nonSourceabilityId = nonSourceabilityId,
                companyId = companyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                qaStatus = qaStatus,
                reason = reason,
                uploaderUserId = uploaderUserId,
                uploadTime = uploadTime,
                reviewerUserId = reviewerUserId,
                qaComment = qaComment,
            )
    }
