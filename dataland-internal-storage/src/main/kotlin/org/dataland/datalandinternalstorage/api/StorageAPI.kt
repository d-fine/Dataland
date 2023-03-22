package org.dataland.datalandinternalstorage.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

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
        value = ["/data/{dataId}"],
        produces = ["application/json"],
    )
    fun selectDataById(@PathVariable("dataId") dataId: String, correlationId: String): ResponseEntity<String>

    /**
     * A method to retrieve blobs from the internal storage using the blobs id
     * @param blobId the id of the data stored in the internal storage which should be retrieved
     * @param correlationId the correlation ID of the data get request
     * @return ResponseEntity containing the selected data
     */
    @Operation(
        summary = "Request blobs by its blob id",
        description = "Requests blobs by its blob id",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved blob."),
        ],
    )
    @GetMapping(
        value = ["/blobs/{blobId}"],
        produces = ["application/octet-stream"],
    )
    fun selectBlobById(
        @PathVariable("blobId") blobId: String,
        correlationId: String,
    ): ResponseEntity<InputStreamResource>
}
