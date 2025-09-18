package org.dataland.datasourcingservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataRequestIdParameterRequired
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.DataRequest
import org.dataland.datasourcingservice.model.request.StoredDataRequest
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
     * A method to post a data request to Dataland.
     *
     * @param request includes necessary info for the data request
     * @return response after posting a data request to Dataland
     */
    @Operation(
        summary = "Send a data request",
        description = "A data request for specific frameworks and companies is being sent.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully processed the data request."),
        ],
    )
    @PostMapping(
        value = ["/"],
        produces = ["application/json"],
        consumes = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun createRequest(
        @Valid @RequestBody request: DataRequest,
    ): ResponseEntity<StoredDataRequest>

    /**
     * A method for users to retrieve information about a single data request
     *
     * @param dataRequestId the ID specifiying the data request
     * @return all information associated with the specified request
     */
    @Operation(
        summary = "Get all information associated with the specified data request.",
        description = "Gets all the stored data of a particular dara request.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved data request."),
        ],
    )
    @GetMapping(
        value = ["/{dataRequestId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getRequest(
        @Valid @PathVariable dataRequestId: String,
    ): ResponseEntity<StoredDataRequest>

    /**
     * A method to patch the request state of a data request
     *
     * @param dataRequestId The request id of the data request to patch
     * @param dataRequestPatch The data with which the request is updated
     * @return the modified data request
     */
    @Operation(
        summary = "Updates the state of a given data request.",
        description = "Updates the processing state of a data request given the data request id.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully patched data request."),
        ],
    )
    @PatchMapping(
        value = ["/{dataRequestId}/state"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun patchDataRequestState(
        @DataRequestIdParameterRequired
        @PathVariable("dataRequestId") dataRequestId: String,
        @Valid
        @RequestParam(
            name = "requestState",
            required = true,
        )
        requestState: RequestState,
    ): ResponseEntity<StoredDataRequest>
}
