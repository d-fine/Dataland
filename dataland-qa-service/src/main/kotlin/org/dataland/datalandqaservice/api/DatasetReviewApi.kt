package org.dataland.datalandqaservice.org.dataland.datalandqaservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable

/**
 * Defines the restful dataland dataset review API
 */
interface DatasetReviewApi {
    /**
     * @param datasetReviewId identifier used to uniquely specify data in the data review object
     */
    @Operation(
        summary = "Change the reviewer of a dataset review object.",
        description = "Set yourself as the reviewer of the dataset review object. Other people cannot modify this object.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully set yourself as the reviewer"),
        ],
    )
    @PatchMapping(
        value = ["/{datasetReviewId}/Reviewer"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun setReviewer(
        @PathVariable datasetReviewId: String,
    ): ResponseEntity<String>
}
