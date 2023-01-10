package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.CompanyAssociatedData
import org.dataland.datalandbackend.model.DataMetaInformation
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

/**
 * Defines the restful dataland-backend API regarding data exchange
 */

@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "dataland-api-key")
@SecurityRequirement(name = "default-oauth")
interface DataAPI<T> {
    @Operation(
        summary = "Upload new data set.",
        description = "The uploaded data is added to the data store, the generated data id is returned."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully added data to the data store.")
        ]
    )
    @PostMapping(
        produces = ["application/json"],
        consumes = ["application/json"]
    )
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    /**
     * A method to store data via Dataland into a data store
     * @param companyAssociatedData consisting of the ID of the company and the data to be stored
     * @return meta info about the stored data including the ID of the created entry in the data store
     */
    fun postCompanyAssociatedData(@Valid @RequestBody companyAssociatedData: CompanyAssociatedData<T>):
        ResponseEntity<DataMetaInformation>

    @Operation(
        summary = "Retrieve specific data from the data store.",
        description = "Data identified by the provided data ID is retrieved."
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
    @PreAuthorize("hasRole('ROLE_USER') or @DataManager.isDataSetPublic(#dataId)")
    /**
     * A method to retrieve specific data identified by its ID
     * @param dataId identifier used to uniquely specify data in the data store
     * @return the complete data stored under the provided data ID with the associated company ID
     */
    fun getCompanyAssociatedData(@PathVariable("dataId") dataId: String):
        ResponseEntity<CompanyAssociatedData<T>>
}
