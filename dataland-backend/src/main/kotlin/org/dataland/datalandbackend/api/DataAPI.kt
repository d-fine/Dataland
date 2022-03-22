package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import javax.validation.Valid

/**
 * Defines the restful dataland-backend API regarding data exchange
 */

interface DataAPI<T> {
    /*
    @Operation(
        summary = "Retrieve list of existing data for this data type.",
        description = "List is composed of meta information containing the data ID, the company ID and the data type."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved list of entries.")
        ]
    )
    @GetMapping(
        value = ["/data"],
        produces = ["application/json"]
    )
    /**
     * Returns the meta information (dataId, companyId and dataType) of available data sets
     */

    fun getData(): ResponseEntity<List<DataSetMetaInformation>>
     */

    @Operation(
        summary = "Upload new data set.",
        description = "The uploaded data is sent to the data store, the generated data id is returned."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully added data set to the data store.")
        ]
    )
    @PostMapping(
        value = ["/{companyId}"],
        produces = ["application/json"],
        consumes = ["application/json"]
    )
    /**
     * A method to store a provided data set via dataland into a data store
     * @param companyId the ID of the company the data belongs to
     * @param dataSet a set of data to be stored
     * @return the ID of the created entry in the data store
     */
    fun postData(@PathVariable("companyId") companyId: String, @Valid @RequestBody dataSet: T): ResponseEntity<String>

    @Operation(
        summary = "Retrieve specific data set from the data store.",
        description = "The data set identified via the provided data ID is retrieved."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved data set.")
        ]
    )
    @GetMapping(
        value = ["/{dataId}"],
        produces = ["application/json"]
    )
    /**
     * A method to retrieve a specific data set identified by its ID
     * @param dataId identifier used to uniquely determine the data set in the data store
     * @return the complete data stored under the provided data ID
     */
    fun getDataSet(@PathVariable("dataId") dataId: String): ResponseEntity<T>
}
