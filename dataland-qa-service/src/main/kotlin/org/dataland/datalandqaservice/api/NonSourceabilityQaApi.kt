package org.dataland.datalandqaservice.api

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.model.NonSourceabilityDecisionRequest
import org.dataland.datalandqaservice.model.NonSourceableQaReviewInformation
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * API interface for non-sourceability QA review endpoints (FR-005, FR-006, FR-007).
 */
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
@RequestMapping("/nonSourceable")
interface NonSourceabilityQaApi {
    /**
     * Returns all QA review records filtered by the given parameters.
     */
    @GetMapping(produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_REVIEWER') or hasRole('ROLE_ADMIN')")
    fun getNonSourceableReviews(
        @RequestParam(required = false) companyId: String?,
        @RequestParam(required = false) dataType: String?,
        @RequestParam(required = false) reportingPeriod: String?,
        @RequestParam(required = false) qaStatus: QaStatus?,
        @RequestParam(defaultValue = "10") chunkSize: Int,
        @RequestParam(defaultValue = "0") chunkIndex: Int,
    ): ResponseEntity<List<NonSourceableQaReviewInformation>>

    /**
     * Returns only pending QA review records (the work queue).
     */
    @GetMapping(value = ["/queue"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_REVIEWER') or hasRole('ROLE_ADMIN')")
    fun getNonSourceableQueue(): ResponseEntity<List<NonSourceableQaReviewInformation>>

    /**
     * Posts a QA decision (Accepted or Rejected) for a given non-sourceability entry.
     */
    @PostMapping(value = ["/{nonSourceabilityId}"])
    @PreAuthorize("hasRole('ROLE_REVIEWER') or hasRole('ROLE_ADMIN')")
    fun postNonSourceabilityDecision(
        @PathVariable nonSourceabilityId: String,
        @RequestBody request: NonSourceabilityDecisionRequest,
    ): ResponseEntity<NonSourceableQaReviewInformation>
}
