package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.metainformation.DataPointMetaInformation
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the restful dataland-backend API regarding data point up and downloads.
 */

@RequestMapping("/data-points")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface DataPointApi {
    /**
     * A method to store a data point via Dataland into a data store
     * @param uploadedDataPoint consisting of the triple data point identifier, company ID and reporting period and the actual data
     * @param bypassQa if set to true, the data will be stored without going through the QA process
     * @return meta info about the stored data point including the ID of the created entry in the data store
     */
    @Operation(
        summary = "Upload new data point.",
        description = "The uploaded data point is added to the data store, the generated data id is returned.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully added data to the data store."),
        ],
    )
    @PostMapping(
        produces = ["application/json"],
        consumes = ["application/json"],
    )
    @PreAuthorize(
        "hasRole('ROLE_UPLOADER') or " +
            "(hasRole('ROLE_USER') and " +
            "(@CompanyRoleChecker.hasCurrentUserGivenRoleForCompany(" +
            "#uploadedDataPoint.companyId, " +
            "T(org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRole).CompanyOwner" +
            ") or " +
            "@CompanyRoleChecker.hasCurrentUserGivenRoleForCompany(" +
            "#uploadedDataPoint.companyId, " +
            "T(org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRole).DataUploader" +
            ")" +
            ")" +
            ")",
    )
    fun postDataPoint(
        @Valid @RequestBody
        uploadedDataPoint: UploadedDataPoint,
        @RequestParam(defaultValue = "false") bypassQa: Boolean,
    ): ResponseEntity<DataPointMetaInformation>

    /**
     * A method to retrieve a data point by providing its ID
     * @param dataId the unique identifier for the data point
     * @return the data point identified by the ID
     */
    @Operation(
        summary = "Retrieve data points by ID.",
        description = "A data point identified by its ID is retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved data point."),
        ],
    )
    @GetMapping(
        value = ["/{dataId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER') or @DataPointManager.isCompanyAssociatedWithDataPointMarkedForPublicAccess(#companyId)")
    fun getDataPoint(
        @PathVariable dataId: String,
    ): ResponseEntity<UploadedDataPoint>

    /**
     * A method to retrieve meta-information about a data point by providing its ID
     * @param dataId the unique identifier for the data point
     * @return the meta-information of the data point identified by the ID
     */
    @Operation(
        summary = "Retrieve meta-information about data points by ID.",
        description = "Meta-information about a data point identified by its ID is retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved meta-information about data point."),
        ],
    )
    @GetMapping(
        value = ["/{dataId}/metadata"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER') or @DataPointManager.isCompanyAssociatedWithDataPointMarkedForPublicAccess(#companyId)")
    fun getDataPointMetaInfo(
        @PathVariable dataId: String,
    ): ResponseEntity<DataPointMetaInformation>
}
