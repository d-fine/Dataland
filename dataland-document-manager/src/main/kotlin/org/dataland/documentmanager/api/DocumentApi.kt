package org.dataland.documentmanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.documentmanager.model.DocumentMetaInfo
import org.dataland.documentmanager.model.DocumentMetaInfoPatch
import org.dataland.documentmanager.model.DocumentMetaInfoResponse
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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
     * Upload a document and corresponding meta data
     * @param document a document
     * @param documentMetaInfo document meta info
     * @return returns a documentMetaInfoResponse containing documentId and metadata
     */
    @Operation(
        summary = "Upload a document and metadata.",
        description = "Upload a document and meta information.",
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
    @PreAuthorize("hasRole('ROLE_UPLOADER') or @UserRolesChecker.isCurrentUserCompanyOwnerOrCompanyUploader()")
    fun postDocument(
        @RequestPart("document") document: MultipartFile,
        @RequestPart("documentMetaInfo", required = false) documentMetaInfo: DocumentMetaInfo?,
    ): ResponseEntity<DocumentMetaInfoResponse>

    /**
     * Patch the metadata information of a document. The field values in documentMetaInfoPatch that
     * are not null will replace the corresponding field values in the DocumentMetaInfoEntity object.
     * @param documentId the id of the document whose meta info shall be patched.
     * @param documentMetaInfoPatch an object of type DocumentMetaInfoPatch which holds all field values to patch.
     */
    @Operation(
        summary = "Update the metadata info of a document.",
        description = "Update the metadata info of a document.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully updated the document's meta information.",
            ),
            ApiResponse(
                responseCode = "403",
                description = "You do not have the right to update the document's meta information.",
            ),
            ApiResponse(responseCode = "404", description = "Document Id does not match any stored document."),
        ],
    )
    @PatchMapping(
        value = ["/{documentId}"],
        produces = ["application/json"],
        consumes = ["application/json"],
    )
    @PreAuthorize(
        "hasRole('ROLE_ADMIN') " +
            "or (hasRole('ROLE_UPLOADER') and @UserRolesChecker.isCurrentUserUploaderOfDocument(#documentId))",
    )
    fun patchDocumentMetaInfo(
        @PathVariable("documentId") documentId: String,
        @Valid @RequestBody(required = true) documentMetaInfoPatch: DocumentMetaInfoPatch,
    ): ResponseEntity<DocumentMetaInfoResponse>

    /**
     * Patch the company id list in the stored metainformation of a given document by adding
     * a single new company id.
     * @param documentId the id of the document whose metainfo shall be patched.
     * @param companyId the company id to add.
     */
    @Operation(
        summary = "Extend the list of companyIds related to a document.",
        description = "Extend the list of companyIds related to a document by a single company id.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully updated the companyIds field in the document's meta information.",
            ),
            ApiResponse(
                responseCode = "403",
                description = "You do not have the right to update the companyIds field.",
            ),
            ApiResponse(responseCode = "404", description = "Document Id does not match any stored document."),
        ],
    )
    @PatchMapping(
        value = ["/{documentId}/companies/{companyId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun patchDocumentMetaInfoCompanyIds(
        @PathVariable("documentId") documentId: String,
        @PathVariable("companyId") companyId: String,
    ): ResponseEntity<DocumentMetaInfoResponse>

    /**
     * Checks if a document with a given ID exists
     * @param documentId the ID to check
     */
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
    @PreAuthorize("hasRole('ROLE_USER')")
    fun checkDocument(
        @PathVariable("documentId") documentId: String,
    )

    /**
     * Retrieve a document by its ID
     * @param documentId the ID to check
     */
    @Operation(
        summary = "Receive a document.",
        description = "Receive a document by its ID from internal storage.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully received document.",
                headers = [
                    Header(name = HttpHeaders.CONTENT_DISPOSITION, schema = Schema(type = "string")),
                    Header(
                        name = HttpHeaders.CONTENT_LENGTH,
                        schema = Schema(type = "integer", format = "int64"),
                    ),
                    Header(name = HttpHeaders.CONTENT_TYPE, schema = Schema(type = "string")),
                ],
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
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getDocument(
        @PathVariable("documentId") documentId: String,
    ): ResponseEntity<InputStreamResource>
}
