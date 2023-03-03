package org.dataland.datalandinternalstorage.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
/**
 * Defines the restful internal storage API.
 */
interface StorageAPI {

    /**
     * A method to retrieve data from the internal storage using the dataID
     * @param dataId the ID of the data stored in the internal storage which should be retrieved
     * @param correlationId the correlation ID of the data get request
     * @return ResponseEntity containing the selected data
     */
    @Operation(
        summary = "Request data by id.",
        description = "Requests data by id.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved data."),
        ],
    )
    @GetMapping(
        value = ["/data"],
        produces = ["application/json"],
    )
    fun selectDataById(dataId: String, correlationId: String): ResponseEntity<String>
}
