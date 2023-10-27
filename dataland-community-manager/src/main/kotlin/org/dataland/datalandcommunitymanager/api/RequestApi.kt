package org.dataland.datalandcommunitymanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

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

    /** A method to get all data requests of a specific user.
     * @return all data requests of the user in a list
     */
    @Operation(
        summary = "Get data requests of a specific user.",
        description = "Gets all the stored data request created by the user in the past.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved data requests for the user."),
        ],
    )
    @GetMapping(
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getDataRequestsForUser(): ResponseEntity<List<DataRequestEntity>>
}

// TODO at the very end: check if it works with api keys
