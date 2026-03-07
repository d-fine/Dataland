package org.dataland.datalandcommunitymanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.dataland.datalandcommunitymanager.model.inquiry.InquiryData
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Defines the community manager API for unauthenticated visitor inquiries.
 * This endpoint is publicly accessible — no authentication token is required.
 */
@RequestMapping("/inquiry")
@Tag(name = "Inquiry")
fun interface InquiryApi {
    /**
     * Accepts a contact inquiry from an unauthenticated visitor and notifies the Dataland team via email.
     * @param inquiryData the contact inquiry payload
     * @return 201 Created if the inquiry was received and the notification was dispatched
     */
    @Operation(operationId = "postInquiry", summary = "Submit a contact inquiry")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Inquiry received and notification dispatched"),
            ApiResponse(
                responseCode = "400",
                description = "Validation failed — field-level errors returned",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Notification dispatch failed",
                content = [Content(schema = Schema())],
            ),
        ],
    )
    @PostMapping(
        consumes = ["application/json"],
    )
    fun postInquiry(
        @Valid @RequestBody inquiryData: InquiryData,
    ): ResponseEntity<Unit>
}