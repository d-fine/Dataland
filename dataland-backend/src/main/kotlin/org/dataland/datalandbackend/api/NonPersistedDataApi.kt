package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Defines the restful dataland-backend API regarding data exchange
 */
@RequestMapping("/internal/nonpersisted")
interface NonPersistedDataApi {

    @Operation(
        summary = "Retrieve specific data from the hashmap of the backend.",
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
    /**
     * This method retrieves data entries from the temporary storage
     * @param dataId filters the requested data to a specific entry.
     */
    fun getCompanyAssociatedDataForInternalStorage(@PathVariable("dataId") dataId: String):
        ResponseEntity<String>
}
