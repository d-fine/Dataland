package org.dataland.datalandcommunitymanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.ExtendedStoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequestResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
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
    ): ResponseEntity<BulkDataRequestResponse>

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
    fun getDataRequestsForRequestingUser(): ResponseEntity<List<ExtendedStoredDataRequest>>

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
        value = ["/aggregated"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getAggregatedDataRequests(
        @RequestParam identifierValue: String? = null,
        @RequestParam dataTypes: Set<DataTypeEnum>? = null,
        @RequestParam reportingPeriod: String? = null,
        @RequestParam status: RequestStatus? = null,
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
    ): ResponseEntity<SingleDataRequestResponse>

    /** A method for users to get a data request by its ID.
     * @return the data requests corresponding to the provided ID
     */
    @Operation(
        summary = "Gets a stored data request for a given ID.",
        description = "Gets the stored data request corresponding to the provided data request ID.",
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
    @PreAuthorize("hasRole('ROLE_ADMIN') or @SecurityUtilsService.isUserAskingForOwnRequest(#dataRequestId)")
    fun getDataRequestById(
        @PathVariable dataRequestId: UUID,
    ): ResponseEntity<StoredDataRequest>

    /** A method to patch an existing data request
     * @param dataRequestId The request id of the data request to patch
     * @param requestStatus The new request status to set
     * @param accessStatus The new access status to set
     * @param contacts The new contacts to set
     * @param message The new message to set
     * @param requestPriority The new request priority to set
     * @param adminComment The new admin comment to set
     * @return the modified data request
     */
    @Suppress("LongParameterList")
    @Operation(
        summary = "Updates a data request.",
        description = "Updates status and message history of data request given data request id.",
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
    @PreAuthorize(
        "hasRole('ROLE_ADMIN') or " +
            "(@SecurityUtilsService.isUserAskingForOwnRequest(#dataRequestId) and " +
            "@SecurityUtilsService.isRequestStatusChangeableByUser(#dataRequestId, #requestStatus) and " +
            "@SecurityUtilsService.isNotTryingToPatch(#accessStatus,#requestPriority, #adminComment) and " +
            "@SecurityUtilsService.isRequestMessageHistoryChangeableByUser(" +
            "#dataRequestId, #requestStatus, #contacts, #message)" +
            ") or" +
            "@SecurityUtilsService.isUserCompanyOwnerForRequestId(#dataRequestId) and" +
            "@SecurityUtilsService.isNotTryingToPatch(" +
            "#requestStatus, #contacts, #message, #requestPriority, #adminComment) ",
    )
    fun patchDataRequest(
        @PathVariable dataRequestId: UUID,
        @RequestParam requestStatus: RequestStatus?,
        @RequestParam accessStatus: AccessStatus?,
        @RequestParam contacts: Set<String>?,
        @RequestParam message: String?,
        @RequestParam requestPriority: RequestPriority?,
        @RequestParam adminComment: String?,
    ): ResponseEntity<StoredDataRequest>

    /** A method for searching data requests based on filters.
     * @param dataType If set, only the requests with a data type in dataType are returned
     * @param userId If set, only the requests from this user are returned
     * @param emailAddress If set, only the requests from users which email address partially matches emailAddress are
     *  returned
     * @param adminComment If set, only comments with this substring are returned
     * @param requestStatus If set, only the requests with a request status in requestStatus are returned
     * @param accessStatus If set, only the requests with an access status in accessStatus are returned
     * @param requestPriority If set, only the requests with this priority are returned
     * @param reportingPeriod If set, only the requests with this reportingPeriod are returned
     * @param datalandCompanyId If set, only the requests for this company are returned
     * @param chunkSize Limits the number of returned requests
     * @param chunkIndex The index of the chunked requests
     * @return all filtered data requests in a list
     */
    @Suppress("LongParameterList")
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
        produces = ["application/json"],
    )
    @PreAuthorize(
        "hasRole('ROLE_ADMIN') or" +
            "@SecurityUtilsService.isUserCompanyOwnerForCompanyId(#datalandCompanyId)",
    )
    fun getDataRequests(
        @RequestParam dataType: Set<DataTypeEnum>?,
        @RequestParam userId: String?,
        @RequestParam emailAddress: String?,
        @RequestParam adminComment: String?,
        @RequestParam requestStatus: Set<RequestStatus>?,
        @RequestParam accessStatus: Set<AccessStatus>?,
        @RequestParam requestPriority: Set<RequestPriority>?,
        @RequestParam reportingPeriod: String?,
        @RequestParam datalandCompanyId: String?,
        @RequestParam(defaultValue = "100") chunkSize: Int,
        @RequestParam(defaultValue = "0") chunkIndex: Int,
    ): ResponseEntity<List<ExtendedStoredDataRequest>>

    /** A method to count data requests based on specific filters.
     * @param dataType If set, only the requests with a data type in dataType are counted
     * @param userId If set, only the requests from this user are counted
     * @param emailAddress If set, only the requests from users which email address partially matches emailAddress are
     *  counted
     * @param adminComment If set, only comments with this substring are counted
     * @param requestStatus If set, only the requests with a request status in requestStatus are counted
     * @param accessStatus If set, only the requests with an access status in accessStatus are counted
     * @param requestPriority If set, only the requests with this priority are counted
     * @param reportingPeriod If set, only the requests with this reportingPeriod are counted
     * @param datalandCompanyId If set, only the requests for this company are counted
     * @return The number of requests that match the filter
     */
    @Operation(
        summary = "Get the number of requests based on filters.",
        description = "Get the number of requests based on filters.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved the number of data requests."),
        ],
    )
    @GetMapping(
        value = ["/numberOfRequests"],
        produces = ["application/json"],
    )
    @PreAuthorize(
        "hasRole('ROLE_ADMIN') or" +
            "@SecurityUtilsService.isUserCompanyOwnerForCompanyId(#datalandCompanyId)",
    )
    fun getNumberOfRequests(
        @RequestParam dataType: Set<DataTypeEnum>?,
        @RequestParam userId: String?,
        @RequestParam emailAddress: String?,
        @RequestParam adminComment: String?,
        @RequestParam requestStatus: Set<RequestStatus>?,
        @RequestParam accessStatus: Set<AccessStatus>?,
        @RequestParam requestPriority: Set<RequestPriority>?,
        @RequestParam reportingPeriod: String?,
        @RequestParam datalandCompanyId: String?,
    ): ResponseEntity<Int>

    /**
     * A method to check if the logged-in user can access a specific dataset.
     * The dataset is specified by a companyId, dataType and a reportingPeriod.
     * @param companyId of the company for which the user might have the role
     * @param dataType of the corresponding framework
     * @param reportingPeriod of the dataset
     */
    @Operation(
        summary = "This head request checks whether the logged-in user has access to dataset.",
        description = "This head request checks whether the logged-in user has access to dataset.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "The user can access the dataset."),
            ApiResponse(
                responseCode = "404",
                description = "Either the specified dataset does not exist or the user cannot access the dataset.",
            ),
        ],
    )
    @RequestMapping(
        method = [RequestMethod.HEAD],
        value = ["/dataset-access/{companyId}/{dataType}/{reportingPeriod}/{userId}"],
    )
    fun hasAccessToDataset(
        @PathVariable("companyId") companyId: UUID,
        @PathVariable("dataType") dataType: String,
        @PathVariable("reportingPeriod") reportingPeriod: String,
        @PathVariable("userId") userId: UUID,
    )
}
