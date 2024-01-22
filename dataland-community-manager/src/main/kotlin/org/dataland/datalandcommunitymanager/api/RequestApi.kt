package org.dataland.datalandcommunitymanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.*
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

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
     *
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
    ): List<AggregatedDataRequest>

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
    @PreAuthorize("hasRole('ROLE_USER')")
    fun patchDataRequest(
        @PathVariable dataRequestId: String,
        @RequestParam requestStatus: RequestStatus = RequestStatus.Open
    ): ResponseEntity<StoredDataRequest>
}
