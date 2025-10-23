package org.dataland.datasourcingservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataRequestIdParameterRequired
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.BulkDataRequest
import org.dataland.datasourcingservice.model.request.BulkDataRequestResponse
import org.dataland.datasourcingservice.model.request.ExtendedStoredRequest
import org.dataland.datasourcingservice.model.request.RequestSearchFilter
import org.dataland.datasourcingservice.model.request.SingleRequest
import org.dataland.datasourcingservice.model.request.SingleRequestResponse
import org.dataland.datasourcingservice.model.request.StoredRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * API interface for handling data requests.
 */
@RequestMapping("/requests")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface RequestApi {
    /**
     * A method to post a bulk request to Dataland.
     * @param bulkDataRequest includes necessary info for the bulk request
     * @return response after posting a bulk data request to Dataland
     */
    @Operation(
        summary = "Send a bulk request",
        description = "A bulk of data requests for specific frameworks and companies is being sent.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully processed a bulk of data requests."),
        ],
    )
    @PostMapping(
        value = ["/bulk"],
        produces = ["application/json"],
        consumes = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and (#userId == authentication.userId or #userId == null))")
    fun postBulkDataRequest(
        @Valid @RequestBody
        bulkDataRequest: BulkDataRequest,
        @RequestParam(required = false)
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.USER_ID_DESCRIPTION,
            example = DataSourcingOpenApiDescriptionsAndExamples.USER_ID_EXAMPLE,
        )
        userId: String? = null,
    ): ResponseEntity<BulkDataRequestResponse>

    /**
     * A method to post a data request to Dataland.
     *
     * @param singleRequest includes necessary info for the data request
     * @return response after posting a data request to Dataland
     */
    @Operation(
        summary = "Send a data request",
        description = "A data request for specific frameworks and companies is being sent.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully processed the request."),
            ApiResponse(
                responseCode = "400",
                description = "The request contains invalid data or is a duplicate.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "403",
                description =
                    "You were trying to impersonate another Dataland user. Only admins have the right to do so. " +
                            "To make a request for yourself, leave the userId parameter empty.",
                content = [Content(schema = Schema())],
            ),
        ],
    )
    @PostMapping(
        produces = ["application/json"],
        consumes = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and (#userId == authentication.userId or #userId == null))")
    fun createRequest(
        @Valid @RequestBody singleRequest: SingleRequest,
        @RequestParam(required = false)
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.USER_ID_DESCRIPTION,
            example = DataSourcingOpenApiDescriptionsAndExamples.USER_ID_EXAMPLE,
        )
        userId: String? = null,
    ): ResponseEntity<SingleRequestResponse>

    /**
     * A method for users to retrieve information about a single data request
     *
     * @param dataRequestId the ID specifying the data request
     * @return all information associated with the specified request
     */
    @Operation(
        summary = "Get all information associated with the specified data request.",
        description = "Gets all the stored data of a particular data request.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved request."),
            ApiResponse(
                responseCode = "403",
                description =
                    "The entered request ID does not belong to any of your requests. Only Dataland admins " +
                            "have the right to query other users' requests.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "The specified request ID does not exist.",
                content = [Content(schema = Schema())],
            ),
        ],
    )
    @GetMapping(
        value = ["/{dataRequestId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @SecurityUtilsService.isUserAskingForOwnRequest(#dataRequestId))")
    fun getRequest(
        @Valid @PathVariable dataRequestId: String,
    ): ResponseEntity<StoredRequest>

    /**
     * A method to patch the request state of a data request
     *
     * @param dataRequestId The request id of the data request to patch
     * @param requestState The new request state after patch
     * @return the modified data request
     */
    @Operation(
        summary = "Updates the state of a given data request.",
        description = "Updates the processing state of a data request given the data request id.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully patched request."),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland admins have the right to patch requests.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "The specified request ID does not exist.",
                content = [Content(schema = Schema())],
            ),
        ],
    )
    @PatchMapping(
        value = ["/{dataRequestId}/state"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun patchRequestState(
        @DataRequestIdParameterRequired
        @PathVariable("dataRequestId") dataRequestId: String,
        @Valid
        @RequestParam(
            name = "requestState",
            required = true,
        )
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_STATE_DESCRIPTION,
            example = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_STATE_EXAMPLE,
        )
        requestState: RequestState,
        @RequestParam(
            name = "adminComment",
            required = false,
        )
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.ADMIN_COMMENT_DESCRIPTION,
            example = DataSourcingOpenApiDescriptionsAndExamples.ADMIN_COMMENT_EXAMPLE,
        )
        adminComment: String? = null,
    ): ResponseEntity<StoredRequest>

    /**
     * A method to patch the request priority of a data request
     *
     * @param dataRequestId The request id of the data request to patch
     * @param requestPriority The new request priority after patch
     * @return the modified data request
     */
    @Operation(
        summary = "Updates the priority of a given data request.",
        description = "Updates the priority of a data request given the data request id.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully patched request."),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland admins have the right to patch requests.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "The specified request ID does not exist.",
                content = [Content(schema = Schema())],
            ),
        ],
    )
    @PatchMapping(
        value = ["/{dataRequestId}/priority"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun patchRequestPriority(
        @DataRequestIdParameterRequired
        @PathVariable("dataRequestId") dataRequestId: String,
        @Valid
        @RequestParam(
            name = "requestPriority",
            required = true,
        )
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_PRIORITY_DESCRIPTION,
            example = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_PRIORITY_EXAMPLE,
        )
        requestPriority: RequestPriority,
        @RequestParam(
            name = "adminComment",
            required = false,
        )
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.ADMIN_COMMENT_DESCRIPTION,
            example = DataSourcingOpenApiDescriptionsAndExamples.ADMIN_COMMENT_EXAMPLE,
        )
        adminComment: String? = null,
    ): ResponseEntity<StoredRequest>

    /**
     * Retrieve the history of a Request object by its ID.
     */
    @Operation(
        summary = "Get full history of requests by ID",
        description = "Retrieve the history of a Request object by its unique identifier.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved request history."),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland admins have the right to query request history.",
                content = [Content(array = ArraySchema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "The specified request ID does not exist.",
                content = [Content(array = ArraySchema())],
            ),
        ],
    )
    @GetMapping(value = ["/{dataRequestId}/history"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @SecurityUtilsService.isUserAskingForOwnRequest(#dataRequestId))")
    fun getRequestHistoryById(
        @DataRequestIdParameterRequired
        @PathVariable dataRequestId: String,
    ): ResponseEntity<List<StoredRequest>>

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
    ): ResponseEntity<List<ExtendedStoredRequest>>

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
        value = ["/count"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun postRequestCountQuery(
        @RequestBody
        requestSearchFilter: RequestSearchFilter<String>,
    ): ResponseEntity<Int>

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
        value = ["/user"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getRequestsForRequestingUser(): ResponseEntity<List<ExtendedStoredRequest>>
}
