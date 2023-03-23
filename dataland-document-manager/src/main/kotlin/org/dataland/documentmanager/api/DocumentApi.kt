package org.dataland.documentmanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.documentmanager.model.DocumentExistsResponse
import org.dataland.documentmanager.model.DocumentMetaInfo
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

/**
 * Defines the restful dataland document manager API regarding document exchange
 */
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface DocumentApi {
    /**
     * Upload a document
     * @param pdfDocument a PDF document
     */
    @Operation(
        summary = "Upload a document.",
        description = "Upload a document and receive meta information",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully uploaded document."),
        ],
    )
    @PostMapping(
        value = ["/"],
        produces = ["application/json"],
        consumes = ["multipart/form-data"],
    )
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun postDocument(
        @RequestPart("pdfDocument") pdfDocument: MultipartFile,
    ): ResponseEntity<DocumentMetaInfo>

    /**
     * Checks if a document with a given ID exists
     * @param documentId the ID to check
     */
    @Operation(
        summary = "Check if a document exists.",
        description = "Check for a given documentId (hash) if the document already exists in the database.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully checked document existence."),
        ],
    )
    @GetMapping(
        value = ["/{documentId}/exists"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun checkDocument(
        @PathVariable("documentId") documentId: String,
    ): ResponseEntity<DocumentExistsResponse>

    /**
     * Retrieve a document by its Id
     * @param documentId the Id to check
     */
    @Operation(
        summary = "Receive a document.",
        description = "Receive a document by its Id from internal storage.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully received document."),
        ],
    )
    @GetMapping(
        value = ["/{documentId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getDocument(
        @PathVariable("documentId") documentId: String,
    ): ResponseEntity<InputStreamResource>
}
