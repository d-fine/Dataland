package org.dataland.datalandqaservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.QaServiceOpenApiDescriptionsAndExamples
import org.dataland.datalandqaservice.model.NonSourceableQaReviewInformation
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the non-sourceability QA review API.
 */
@RequestMapping("/nonSourceable")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface NonSourceabilityQaApi {
    /**
     * List non-sourceability QA reviews filtered by company, data type, reporting period, and QA status.
     *
     * @param companyId optional company identifier
     * @param dataType optional data type
     * @param reportingPeriod optional reporting period
     * @param qaStatus optional QA status filter
     * @param showOnlyActive whether to show only active (non-rejected) reviews
     * @param chunkSize the number of records per page
     * @param chunkIndex the page index
     * @return a list of non-sourceability QA review information
     */
    @Operation(
        summary = "List non-sourceability QA reviews",
        description = "Returns QA review records for non-sourceability requests filtered by dimensions and QA status.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved non-sourceability QA review records."),
        ],
    )
    @GetMapping(produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_REVIEWER') or hasRole('ROLE_JUDGE')")
    fun getNonSourceabilityReviews(
        @RequestParam(required = false)
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
            required = false,
        )
        companyId: String? = null,
        @RequestParam(required = false)
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
            required = false,
        )
        dataType: String? = null,
        @RequestParam(required = false)
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
            required = false,
        )
        reportingPeriod: String? = null,
        @RequestParam(required = false)
        @Parameter(
            description = QaServiceOpenApiDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
            required = false,
        )
        qaStatus: QaStatus? = null,
        @RequestParam(defaultValue = "true")
        showOnlyActive: Boolean,
        @RequestParam(defaultValue = "10")
        chunkSize: Int,
        @RequestParam(defaultValue = "0")
        chunkIndex: Int,
    ): ResponseEntity<List<NonSourceableQaReviewInformation>>

    /**
     * Get the pending non-sourceability QA review queue in chronological order.
     *
     * @return a list of pending non-sourceability QA review information
     */
    @Operation(
        summary = "Get pending non-sourceability QA queue",
        description = "Returns pending non-sourceability QA review records in chronological order.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved pending non-sourceability QA queue."),
        ],
    )
    @GetMapping(value = ["/queue"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_REVIEWER') or hasRole('ROLE_JUDGE')")
    fun getPendingNonSourceabilityQueue(): ResponseEntity<List<NonSourceableQaReviewInformation>>

    /**
     * Record a non-sourceability QA decision (Accepted or Rejected).
     *
     * @param nonSourceabilityId the canonical non-sourceability record id
     * @param qaStatus the QA decision status
     * @return the updated non-sourceability QA review information
     */
    @Operation(
        summary = "Record a non-sourceability QA decision",
        description = "Records Accepted or Rejected QA decision for a non-sourceability review and emits lifecycle event.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully recorded non-sourceability QA decision."),
        ],
    )
    @PostMapping(value = ["/{nonSourceabilityId}"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_REVIEWER')")
    fun decideNonSourceability(
        @PathVariable
        @Parameter(
            description = "Canonical non-sourceability record id.",
            example = GeneralOpenApiDescriptionsAndExamples.GENERAL_UUID_EXAMPLE,
            required = true,
        )
        nonSourceabilityId: String,
        @RequestParam
        @Parameter(
            description = QaServiceOpenApiDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
            required = true,
        )
        qaStatus: QaStatus,
        @RequestParam(required = false)
        @Parameter(
            description = QaServiceOpenApiDescriptionsAndExamples.COMMENT_DESCRIPTION,
            required = false,
        )
        qaComment: String? = null,
    ): ResponseEntity<NonSourceableQaReviewInformation>
}
