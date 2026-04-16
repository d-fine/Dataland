package org.dataland.datalandqaservice.controller

import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.api.NonSourceabilityQaApi
import org.dataland.datalandqaservice.model.NonSourceabilityDecisionRequest
import org.dataland.datalandqaservice.model.NonSourceableQaReviewInformation
import org.dataland.datalandqaservice.services.NonSourceabilityQaReviewManager
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID.randomUUID

/**
 * Controller for non-sourceability QA review endpoints (FR-005, FR-006, FR-007, FR-009).
 */
@RestController
class NonSourceabilityQaController(
    @Autowired private val nonSourceabilityQaReviewManager: NonSourceabilityQaReviewManager,
) : NonSourceabilityQaApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getNonSourceableReviews(
        companyId: String?,
        dataType: String?,
        reportingPeriod: String?,
        qaStatus: QaStatus?,
        chunkSize: Int,
        chunkIndex: Int,
    ): ResponseEntity<List<NonSourceableQaReviewInformation>> {
        logger.info("GET /nonSourceable (companyId=$companyId, dataType=$dataType, reportingPeriod=$reportingPeriod, qaStatus=$qaStatus)")
        return ResponseEntity.ok(
            nonSourceabilityQaReviewManager.getReviews(companyId, dataType, reportingPeriod, qaStatus, chunkSize, chunkIndex),
        )
    }

    override fun getNonSourceableQueue(): ResponseEntity<List<NonSourceableQaReviewInformation>> {
        logger.info("GET /nonSourceable/queue")
        return ResponseEntity.ok(nonSourceabilityQaReviewManager.getQueue())
    }

    override fun postNonSourceabilityDecision(
        nonSourceabilityId: String,
        request: NonSourceabilityDecisionRequest,
    ): ResponseEntity<NonSourceableQaReviewInformation> {
        val reviewerUserId = DatalandAuthentication.fromContext().userId
        val correlationId = randomUUID().toString()
        logger.info(
            "POST /nonSourceable/$nonSourceabilityId qaStatus=${request.qaStatus} (correlationId=$correlationId)",
        )
        val result =
            nonSourceabilityQaReviewManager.postDecision(
                nonSourceabilityId = nonSourceabilityId,
                qaStatus = request.qaStatus,
                qaComment = request.qaComment,
                reviewerUserId = reviewerUserId,
                correlationId = correlationId,
            )
        return ResponseEntity.ok(result)
    }
}
