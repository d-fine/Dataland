package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.StorableDataSet
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
    // Todo: revisit the docs and comments

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
        // Todo: change to the appropriate return type
    ): ResponseEntity<String>

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
    // Todo: revisit the required roles
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getDataPoint(
        @PathVariable dataId: String,
        // Todo: change to the appropriate return type
    ): ResponseEntity<StorableDataSet>
}
