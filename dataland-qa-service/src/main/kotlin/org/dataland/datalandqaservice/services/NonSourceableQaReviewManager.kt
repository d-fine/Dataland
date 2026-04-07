package org.dataland.datalandqaservice.services

import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.messages.NonSourceabilityCreatedEventPayload
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.NonSourceableQaReviewInformationEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.NonSourceableQaReviewInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.NonSourceableQaReviewRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Manages persistence and retrieval of non-sourceable QA review items.
 */
@Service
class NonSourceableQaReviewManager
    @Autowired
    constructor(
        private val nonSourceableQaReviewRepository: NonSourceableQaReviewRepository,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Creates a pending non-sourceable QA review item from a backend created event.
         * If the review item already exists, the method returns without creating a duplicate.
         */
        @Transactional
        fun createReviewItemFromCreatedEvent(
            payload: NonSourceabilityCreatedEventPayload,
            correlationId: String,
        ) {
            val existing = nonSourceableQaReviewRepository.findByNonSourceabilityId(payload.nonSourceabilityId)
            if (existing != null) {
                logger.info(
                    "Skipping duplicate non-sourceable QA review creation for nonSourceabilityId ${payload.nonSourceabilityId} " +
                        "(correlationId: $correlationId)",
                )
                return
            }

            val uploadTime = payload.uploadTime.toInstant().toEpochMilli()
            val entity =
                NonSourceableQaReviewInformationEntity(
                    id = null,
                    nonSourceabilityId = payload.nonSourceabilityId,
                    companyId = payload.companyId.toString(),
                    dataType = payload.dataType,
                    reportingPeriod = payload.reportingPeriod,
                    reason = payload.reason,
                    uploaderUserId = payload.uploaderUserId,
                    uploadTime = uploadTime,
                    qaStatus = QaStatus.Pending,
                    reviewerUserId = null,
                    qaComment = null,
                    createdAt = payload.eventPublishedTime.toInstant().toEpochMilli(),
                    updatedAt = payload.eventPublishedTime.toInstant().toEpochMilli(),
                )
            nonSourceableQaReviewRepository.save(entity)

            logger.info(
                "Created non-sourceable QA review item for nonSourceabilityId ${payload.nonSourceabilityId} " +
                    "(correlationId: $correlationId)",
            )
        }

        /**
         * Returns non-sourceable QA review items matching optional filters.
         */
        @Transactional(readOnly = true)
        fun getNonSourceableReviews(
            companyId: String,
            dataType: String?,
            reportingPeriod: String?,
            qaStatus: QaStatus?,
        ): List<NonSourceableQaReviewInformation> =
            nonSourceableQaReviewRepository
                .findByFilter(companyId, dataType, reportingPeriod, qaStatus)
                .map { it.toApiModel() }

        /**
         * Returns pending non-sourceable QA review queue items (oldest first).
         */
        @Transactional(readOnly = true)
        fun getPendingNonSourceableReviewQueue(): List<NonSourceableQaReviewInformation> =
            nonSourceableQaReviewRepository
                .findPendingReviewQueue()
                .map { it.toApiModel() }
    }
