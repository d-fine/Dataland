package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.dataland.datalandbackend.model.CompanyAssociatedDataSet
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
    @Operation(
        summary = "Upload new data set.",
        description = "The uploaded data is added to the data store, the generated data id is returned."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully added data set to the data store.")
        ]
    )
    @PostMapping(
        produces = ["application/json"],
        consumes = ["application/json"]
    )
    /**
     * A method to store a provided data set via dataland into the data store
     * @param companyAssociatedDataSet consisting of the ID of the company and the data to be stored
     * @return the ID of the created entry in the data store
     */
    fun postCompanyAssociatedDataSet(@Valid @RequestBody companyAssociatedDataSet: CompanyAssociatedDataSet<T>):
        ResponseEntity<String>

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
     * @return the complete data stored under the provided data ID with the associated company ID
     */
    fun getCompanyAssociatedDataSet(@PathVariable("dataId") dataId: String):
        ResponseEntity<CompanyAssociatedDataSet<T>>
}
