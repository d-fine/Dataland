package org.dataland.datalandqaservice.controller

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.NonSourceableQaReviewInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReviewManager
import org.dataland.datalandqaservice.services.NonSourceableQaReviewManager
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import java.util.UUID.randomUUID

/**
 * Controller for non-sourceable QA review endpoints
 */
@RestController
class NonSourceableQaController(
    @Autowired var qaReviewManager: QaReviewManager,
    @Autowired var nonSourceableQaReviewManager: NonSourceableQaReviewManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Retrieves non-sourceable QA reviews for a specific company with optional filters.
     */
    @GetMapping(value = ["/qa/nonSourceable"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_REVIEWER') or hasRole('ROLE_JUDGE')")
    fun getNonSourceableReviews(
        @RequestParam companyId: String,
        @RequestParam(required = false) dataType: String?,
        @RequestParam(required = false) reportingPeriod: String?,
        @RequestParam(required = false) qaStatus: QaStatus?,
    ): ResponseEntity<List<NonSourceableQaReviewInformation>> {
        logger.info("Received request to retrieve non-sourceable QA reviews for companyId $companyId")
        return ResponseEntity.ok(
            nonSourceableQaReviewManager.getNonSourceableReviews(
                companyId = companyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                qaStatus = qaStatus,
            ),
        )
    }

    /**
     * Retrieves pending non-sourceable QA reviews queue.
     */
    @GetMapping(value = ["/qa/nonSourceable/queue"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_REVIEWER') or hasRole('ROLE_JUDGE')")
    fun getNonSourceableReviewQueue(): ResponseEntity<List<NonSourceableQaReviewInformation>> {
        logger.info("Received request to retrieve non-sourceable QA review queue")
        return ResponseEntity.ok(nonSourceableQaReviewManager.getPendingNonSourceableReviewQueue())
    }

    /**
     * Submits a QA decision for a non-sourceability review item.
     */
    @PostMapping(value = ["/qa/nonSourceable/{nonSourceabilityId}"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_REVIEWER') or hasRole('ROLE_JUDGE')")
    fun submitNonSourceableDecision(
        @PathVariable nonSourceabilityId: UUID,
        @RequestParam qaStatus: QaStatus,
        @RequestParam(required = false) qaComment: String?,
    ): ResponseEntity<NonSourceableQaReviewInformation> {
        if (qaStatus != QaStatus.Accepted && qaStatus != QaStatus.Rejected) {
            throw InvalidInputApiException(
                "Invalid qaStatus for non-sourceable decision.",
                "qaStatus must be Accepted or Rejected.",
            )
        }

        val reviewerId = DatalandAuthentication.fromContext().userId
        val correlationId = randomUUID().toString()
        logger.info(
            "Received non-sourceable decision $qaStatus for nonSourceabilityId $nonSourceabilityId " +
                "from user $reviewerId (correlationId: $correlationId)",
        )

        val updatedReview =
            qaReviewManager.handleNonSourceabilityDecision(
                nonSourceabilityId = nonSourceabilityId,
                qaStatus = qaStatus,
                reviewerUserId = reviewerId,
                qaComment = qaComment,
                correlationId = correlationId,
            )
        return ResponseEntity.ok(updatedReview)
    }
}
