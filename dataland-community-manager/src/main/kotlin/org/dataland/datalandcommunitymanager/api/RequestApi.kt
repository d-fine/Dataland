package org.dataland.datalandcommunitymanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

/**
 * Defines the restful dataland-community-manager API regarding.
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
    @PreAuthorize("hasRole('ROLE_USER')")
    fun postBulkDataRequest(
        @Valid @RequestBody
        bulkDataRequest: BulkDataRequest,
    ):
        ResponseEntity<BulkDataRequestResponse>

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
    fun getDataRequestsForUser(): ResponseEntity<List<StoredDataRequest>>

    /** Retrieves aggregated data requests by aggregating all userIds
     * @return aggregated data requests that match the given filters
     */
    @Operation(
        summary = "Get aggregated data requests.",
        description = "Gets all data requests that match the given filters, while aggregating userIDs.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved aggregated data requests."),
        ],
    )
    @GetMapping(
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getAggregatedDataRequests(
        @RequestParam identifierValue: String? = null,
        @RequestParam dataTypes: Set<DataTypeEnum>? = null,
        @RequestParam reportingPeriod: String? = null,
    ): ResponseEntity<List<AggregatedDataRequest>>

    /**
     * A method to post a single request to Dataland.
     * @param singleDataRequest includes necessary info for the single request
     * @return response after posting a single data request to Dataland
     *
     */
    @Operation(
        summary = "Send a single request",
        description = "A single of data requests for specific frameworks and companies is being sent.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully processed a single data request."),
        ],
    )
    @PostMapping(
        value = ["/single"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun postSingleDataRequest(
        @Valid @RequestBody
        singleDataRequest: SingleDataRequest,
    ):
        ResponseEntity<List<StoredDataRequest>>

    /** A method for users to get a data request by its ID.
     * @return the data requests corresponding to the provided ID
     */
    @Operation(
        summary = "Get all stored data requests of the user making the request.",
        description = "Gets all the stored data request created by the user who is making the request.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved data request for the provided ID."),
        ],
    )
    @GetMapping(
        value = ["/{dataRequestId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getDataRequestById(@PathVariable dataRequestId: UUID): ResponseEntity<StoredDataRequest>

    /** Changes request status of existing data request
     * @return the modified data request
     */

    @Operation(
        summary = "Update status of data request.",
        description = "Updates status of data request given data request id.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully patched data request."),
        ],
    )
    @PatchMapping(
        value = ["/{dataRequestId}/requestStatus"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun patchDataRequest(
        @PathVariable dataRequestId: UUID,
        @RequestParam requestStatus: RequestStatus = RequestStatus.Open,
    ): ResponseEntity<StoredDataRequest>

    /** A method for searching data requests based on filters.
     * @return all filtered data requests in a list
     */
    @Operation(
        summary = "Get all stored data requests based on filters.",
        description = "Gets all the stored data request based on filters.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved data requests"),
        ],
    )
    @GetMapping(
        value = ["/all"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getDataRequests(
        @RequestParam dataType: DataTypeEnum?,
        @RequestParam userId: String?,
        @RequestParam requestStatus: RequestStatus?,
        @RequestParam reportingPeriod: String?,
        @RequestParam dataRequestCompanyIdentifierValue: String?,
    ): ResponseEntity<List<StoredDataRequest>>
}
