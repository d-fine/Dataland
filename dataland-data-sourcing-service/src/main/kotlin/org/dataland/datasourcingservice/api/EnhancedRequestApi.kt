package org.dataland.datasourcingservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datasourcingservice.model.enhanced.DataSourcingEnhancedRequest
import org.dataland.datasourcingservice.model.enhanced.RequestSearchFilter
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * API interface for handling requests enhanced by data sourcing details.
 */
@RequestMapping("/enhanced-requests")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface EnhancedRequestApi {
    /**
     * Search requests by filters.
     */
    @Operation(
        summary = "Search requests by filters.",
        description =
            "Search all requests in the data sourcing service, optionally filtering by company ID, data type, reporting period or state.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved requests."),
            ApiResponse(
                responseCode = "400",
                description = "At least one of your provided filters is not of the correct format.",
                content = [Content(array = ArraySchema())],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland admins have the right to search among all requests.",
                content = [Content(array = ArraySchema())],
            ),
        ],
    )
    @PostMapping(
        value = ["/search"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun postRequestSearch(
        @RequestBody
        requestSearchFilter: RequestSearchFilter<String>,
        @RequestParam(defaultValue = "100")
        chunkSize: Int,
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.CHUNK_INDEX_DESCRIPTION,
            required = false,
        )
        @RequestParam(defaultValue = "0")
        chunkIndex: Int,
    ): ResponseEntity<List<DataSourcingEnhancedRequest>>

    /** A method for users to get all their existing data requests.
     * @return all data requests of the user in a list
     */
    @Operation(
        summary = "Get all stored data requests of the user making the request.",
        description = "Gets all the stored data request created by the user who is making the request.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved data requests for the user."),
        ],
    )
    @GetMapping(
        value = ["/"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getRequestsForRequestingUser(): ResponseEntity<List<DataSourcingEnhancedRequest>>

    /**
     * Get the number of requests based on filters.
     */
    @Operation(
        summary = "Get the number of requests based on filters.",
        description =
            "Retrieve the number of requests stored in the data sourcing service, optionally filtering by company ID, " +
                "data type, reporting period or state.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully queried the number of requests."),
            ApiResponse(
                responseCode = "400",
                description = "At least one of your provided filters is not of the correct format.",
                content = [Content(array = ArraySchema())],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland admins have the right to query the number of requests.",
                content = [Content(array = ArraySchema())],
            ),
        ],
    )
    @PostMapping(
        value = ["/search/count"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun postRequestCountQuery(
        @RequestBody
        requestSearchFilter: RequestSearchFilter<String>,
    ): ResponseEntity<Int>
}
