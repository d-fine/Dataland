package org.dataland.datalandinternalstorage.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.dataland.datalandinternalstorage.model.StorableDataPoint
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

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
        summary = "Request data by ID.",
        description = "Requests data by ID.",
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
    fun selectDataById(
        @PathVariable("dataId") dataId: String,
        correlationId: String,
    ): ResponseEntity<String>

    /**
     * A method to retrieve blobs from the internal storage using the blobs ID
     * @param blobId the ID of the data stored in the internal storage which should be retrieved
     * @param correlationId the correlation ID of the data get request
     * @return ResponseEntity containing the selected data
     */
    @Operation(
        summary = "Request blobs by its blob ID",
        description = "Requests blobs by its blob ID",
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

    /**
     * A method to retrieve multiple data points from the internal storage using the [dataIds]
     * @param dataIds the IDs of the data points stored in the internal storage which should be retrieved
     * @param correlationId the correlation ID of the data get request
     * @return ResponseEntity containing the selected data
     */
    @Operation(
        summary = "Request data points by IDs.",
        description = "Requests data points by IDs.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved data point."),
        ],
    )
    @PostMapping(
        value = ["/data/data-points/get-batch"],
        produces = ["application/json"],
    )
    fun selectBatchDataPointsByIds(
        @RequestBody dataIds: List<String>,
        correlationId: String,
    ): ResponseEntity<Map<String, StorableDataPoint>>

    /**
     * A method to check which data is associated to a given document ID
     * @param documentId the ID of the document
     * @param correlationId the correlation ID of the data get request
     * @return ResponseEntity containing associated data points and dataset IDs
     */
    @Operation(
        summary = "Get document references.",
        description = "Gets data point IDs and dataset IDs that reference this document.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved list of data point IDs and dataset IDs."),
        ],
    )
    @GetMapping(
        value = ["/documents/{documentId}/references"],
        produces = ["application/json"],
    )
    fun getDocumentReferences(
        @PathVariable("documentId") documentId: String,
        correlationId: String,
    ): ResponseEntity<Map<String, List<String>>>
}
