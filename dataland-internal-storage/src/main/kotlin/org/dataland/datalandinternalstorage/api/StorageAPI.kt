package org.dataland.datalandinternalstorage.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.dataland.datalandinternalstorage.models.InsertDataResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

/**
 * Defines the restful internal storage API.
 */
interface StorageAPI {

    @Operation(
        summary = "Request data by id.",
        description = "Requests data by id."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved data.")
        ]
    )
    @GetMapping(
        value = ["/data"],
        produces = ["application/json"]
    )
    /**
     * A method to retrieve data from the internal storage using the dataID
     * @param dataId the ID of the data stored in the internal storage which should be retrieved
     * @param correlationId the correlation ID of the data get request
     * @return ResponseEntity containing the selected data
     */
    fun selectDataById(dataId: String, correlationId: String): ResponseEntity<String>

    @Operation(
        summary = "Upload data.",
        description = "Upload data and get data id."
    )
    @ApiResponses(
        value = [ApiResponse(responseCode = "200", description = "Successfully retrieved data.")]
    )
    @PostMapping(
        value = ["/data"]
    )
    /**
     * A method to store data in the internal storage
     * @param correlationId the correlation ID of the data post request
     * @param body the data stored body to be stored
     */
    fun insertData(correlationId: String, @RequestBody(required = true) body: String):
        ResponseEntity<InsertDataResponse>
}
