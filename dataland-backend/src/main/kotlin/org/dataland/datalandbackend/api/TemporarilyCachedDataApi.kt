package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Defines the restful dataland-backend API regarding internal data exchange
 */
@RequestMapping("/internal/cached")
interface TemporarilyCachedDataApi {
    /**
     * This method retrieves data entries from the temporary storage
     * @param dataId filters the requested data to a specific entry.
     */
    @Operation(
        summary = "Retrieve specific data from the cache store of the backend.",
        description = "Data identified by the provided data ID is retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved dataset."),
        ],
    )
    @GetMapping(
        value = ["/public/{dataId}"],
        produces = ["application/json"],
    )
    fun getReceivedPublicData(
        @PathVariable("dataId") dataId: String,
    ): ResponseEntity<String>

    /**
     * Retrieve batched data from the internal cache
     * @param dataId filters the requested data to a specific entry.
     */
    @Operation(
        summary = "Retrieve specific data from the cache store of the backend.",
        description = "Data identified by the provided data IDs is retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved dataset."),
        ],
    )
    @PostMapping(
        value = ["/public/get-batch"],
        produces = ["application/json"],
    )
    fun getBatchReceivedPublicData(
        @RequestBody dataId: List<String>,
    ): ResponseEntity<Map<String, String>>

    /**
     * This method retrieves private data entries from the temporary storage
     * @param dataId filters the requested data to a specific entry.
     */
    @Operation(
        summary = "Retrieve specific data from the cache store of the backend.",
        description = "Data identified by the provided data ID is retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved dataset."),
        ],
    )
    @GetMapping(
        value = ["/private/{dataId}"],
        produces = ["application/json"],
    )
    fun getReceivedPrivateJson(
        @PathVariable("dataId") dataId: String,
    ): ResponseEntity<String>

    /**
     * This method retrieves data entries from the temporary storage
     * @param hash filters the requested data to a specific entry.
     */
    @Operation(
        summary = "Retrieve specific data from the cache store of the backend.",
        description = "Data identified by the provided sha256 hash is retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved blob."),
        ],
    )
    @GetMapping(
        value = ["/private/document/{hash}"],
        produces = ["application/octet-stream"],
    )
    fun getReceivedPrivateDocument(
        @PathVariable("hash") hash: String,
    ): ResponseEntity<InputStreamResource>
}
