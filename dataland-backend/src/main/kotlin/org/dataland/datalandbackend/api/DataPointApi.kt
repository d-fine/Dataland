package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.StorableDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadableDataPoint
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the restful dataland-backend API regarding meta data searches.
 */

@RequestMapping("/datapoints")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface DataPointApi {
    /**
     * A method to store data via Dataland into a data store
     * @param uploadedDataPoint consisting of the triple data type company ID and reporting period and the actual data
     * @param bypassQa if set to true, the data will be stored without going through the QA process
     * @return meta info about the stored data point including the ID of the created entry in the data store
     */
    @Operation(
        summary = "Upload new data set.",
        description = "The uploaded data is added to the data store, the generated data id is returned.",
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
    // ToDo: revisit the required roles
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun postDataPoint(
        @Valid @RequestBody
        uploadedDataPoint: UploadableDataPoint,
        @RequestParam(defaultValue = "false") bypassQa: Boolean,
    ): ResponseEntity<String>
    /*/**
     * A method to search for meta info about data sets registered by Dataland
     * @param companyId if set, filters the requested meta info to a specific company.
     * @param dataType if set, filters the requested meta info to a specific data type.
     * @param showOnlyActive if set to true or empty, only metadata of QA reports are returned that are active.
     *   If set to false, all QA reports will be returned regardless of their active status.
     * @param reportingPeriod if set, the method only returns meta info with this reporting period
     * @param uploaderUserIds if set, the method will only return meta info for datasets uploaded by these user ids.
     * @param qaStatus if set, the method only returns meta info for datasets with this qa status
     * @return a list of matching DataMetaInformation
     */
    @Operation(
        summary = "Search in Dataland for meta info about data.",
        description = "Meta info about data sets registered by Dataland can be retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved meta info."),
        ],
    )
    @GetMapping(
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getListOfDataPoints(
        @RequestParam companyId: String? = null,
        @RequestParam dataType: DataType? = null,
        @RequestParam reportingPeriod: String? = null,
        @RequestParam(defaultValue = "true") showOnlyActive: Boolean,
    ): ResponseEntity<List<DataMetaInformation>>*/

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
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getDataPoint(
        @PathVariable dataId: String,
    ): ResponseEntity<StorableDataPoint>
}
