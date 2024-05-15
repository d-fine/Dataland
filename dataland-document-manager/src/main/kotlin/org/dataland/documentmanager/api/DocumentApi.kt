package org.dataland.documentmanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.documentmanager.model.DocumentUploadResponse
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
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
     * @param document a document
     */
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
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
    fun postDocument(
        @RequestPart document: MultipartFile,
    ): ResponseEntity<DocumentUploadResponse>

    /**
     * Checks if a document with a given ID exists
     * @param documentId the ID to check
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(
        summary = "Check if a document exists.",
        description = "Check for a given document ID (hash) if the document already exists in the database.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully checked document existence."),
            ApiResponse(responseCode = "404", description = "Successfully checked that a document does not exist."),
        ],
    )
    @RequestMapping(
        method = [RequestMethod.HEAD],
        value = ["/{documentId}"],
        produces = ["application/json"],
    )
    fun checkDocument(
        @PathVariable("documentId") documentId: String,
    )

    /**
     * Checks if a document with a given ID exists and returns a boolean
     * @param documentId the ID to check
     * @return boolean value
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(
        summary = "Check if a document exists.",
        description = "Check for a given document ID (hash) if the document already exists in the database.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully checked document existence."),
            ApiResponse(responseCode = "404", description = "Successfully checked that a document does not exist."),
        ],
    )
    @RequestMapping(
        method = [RequestMethod.HEAD],
        value = ["/{documentId}"],
        produces = ["application/json"],
    )
    fun checkDocumentBoolean(
        @PathVariable("documentId") documentId: String,
    ): ResponseEntity<Boolean>

    /**
     * Retrieve a document by its ID
     * @param documentId the ID to check
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(
        summary = "Receive a document.",
        description = "Receive a document by its ID from internal storage.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully received document.",
                headers = [Header(name = HttpHeaders.CONTENT_DISPOSITION, schema = Schema(type = "string"))],
            ),
        ],
    )
    @GetMapping(
        value = ["/{documentId}"],
        produces = [
            "application/json",
            "application/pdf",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.oasis.opendocument.spreadsheet",
        ],
    )
    fun getDocument(
        @PathVariable("documentId") documentId: String,
    ): ResponseEntity<InputStreamResource>
}
