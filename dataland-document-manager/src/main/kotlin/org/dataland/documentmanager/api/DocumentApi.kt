package org.dataland.documentmanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.documentmanager.model.DocumentMetaInfo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

/**
 * Defines the restful dataland document manager API regarding data exchange
 */
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface DocumentApi {
    /**
     * A method to retrieve a document by its ID
     * @param documentId the ID of the document to be retrieved
     * @return the document retrieved
     */
    @Operation(
        summary = "Receive a document by ID.",
        description = "The document with the given ID is returned.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved document."),
        ],
    )
    @GetMapping(
        value = ["/a/{documentId}"],
        produces = ["application/json"],
    )
    fun getDocument(
        @PathVariable("documentId") documentId: String,
    ): ResponseEntity<DocumentMetaInfo>
}
