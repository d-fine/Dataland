package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID.randomUUID

/**
 * Service responsible for orchestrating the change of QA status for a dataset, which includes
 * changing the QA status of the dataset itself and all its data points. This service delegates
 * to QaReviewManager and DataPointQaReviewManager for the actual handling of the QA status change.
 */
@Service
class QaStatusService
    @Autowired
    constructor(
        private val qaReviewManager: QaReviewManager,
        private val dataPointQaReviewManager: DataPointQaReviewManager,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Orchestrates changing QA status for a dataset: delegates to QaReviewManager and
         * DataPointQaReviewManager. Returns the generated correlationId for tracing.
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

            qaReviewManager.handleQaChange(
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
