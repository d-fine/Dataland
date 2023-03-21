package org.dataland.documentmanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Defines the restful dataland-document-manager API regarding internal data exchange
 */
@RequestMapping("/internal/cached")
interface TemporarilyCachedDocumentApi {
    /**
     * This method retrieves data entries from the temporary storage
     * @param sha256hash filters the requested data to a specific entry.
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
        value = ["/{sha256hash}"],
        produces = ["application/octet-stream"],
    )
    fun getReceivedData(@PathVariable("sha256hash") sha256hash: String):
        ResponseEntity<ByteArray>
}
