package org.dataland.documentmanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackendutils.model.DocumentCategory
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CompanyIdParameterRequired
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DocumentManagerOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.documentmanager.entities.DocumentMetaInfoEntity
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
import org.springframework.web.bind.annotation.RequestParam
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
        summary = "Upload a document and its metainformation.",
        description =
            "Upload a document and (optionally) its metainformation. When specifying the metainformation, " +
                "the fields 'publicationDate' and 'reportingPeriod' are optional.",
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
        summary = "Update the metainformation of a document.",
        description =
            "Update the metainformation of a document. All fields are optional, and only mentioned " +
                "fields will be overwritten.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully updated the document's metainformation.",
            ),
            ApiResponse(
                responseCode = "403",
                description = "You do not have the right to update the document's metainformation.",
                content = [
                    Content(
                        schema = Schema(),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Document ID does not match any stored document.",
                content = [
                    Content(
                        schema = Schema(),
                    ),
                ],
            ),
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
        @Parameter(
            name = "documentId",
            description = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_ID_DESCRIPTION,
            example = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_ID_EXAMPLE,
            required = true,
        )
        @PathVariable("documentId")
        documentId: String,
        @Valid @RequestBody(required = true) documentMetaInfoPatch: DocumentMetaInfoPatch,
    ): ResponseEntity<DocumentMetaInfoResponse>

    /**
     * Patch the company ID list in the stored metainformation of a given document by adding
     * a single new company id.
     * @param documentId the id of the document whose metainfo shall be patched.
     * @param companyId the company id to add.
     */
    @Operation(
        summary = "Extend the list of company IDs related to a document.",
        description = "Extend the list of company IDs related to a document by a single company id.",
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
                content = [
                    Content(
                        schema = Schema(),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Document ID does not match any stored document.",
                content = [
                    Content(
                        schema = Schema(),
                    ),
                ],
            ),
        ],
    )
    @PatchMapping(
        value = ["/{documentId}/companies/{companyId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun patchDocumentMetaInfoCompanyIds(
        @Parameter(
            name = "documentId",
            description = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_ID_DESCRIPTION,
            example = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_ID_EXAMPLE,
            required = true,
        )
        @PathVariable("documentId")
        documentId: String,
        @CompanyIdParameterRequired
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
        @Parameter(
            name = "documentId",
            description = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_ID_DESCRIPTION,
            example = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_ID_EXAMPLE,
            required = true,
        )
        @PathVariable("documentId")
        documentId: String,
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
        @Parameter(
            name = "documentId",
            description = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_ID_DESCRIPTION,
            example = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_ID_EXAMPLE,
            required = true,
        )
        @PathVariable("documentId")
        documentId: String,
    ): ResponseEntity<InputStreamResource>

    /**
     * Retrieve document meta information by document ID
     * @param documentId the ID for which to retrieve meta information
     */
    @Operation(
        summary = "Receive metainformation for a document.",
        description = "Receive metainformation for a document by its ID from internal storage.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully received document meta information.",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Document meta information could not be retrieved.",
                content = [
                    Content(
                        schema = Schema(),
                    ),
                ],
            ),
        ],
    )
    @GetMapping(
        value = ["/{documentId}/metadata"],
        produces = [
            "application/json",
        ],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getDocumentMetaInformation(
        @Parameter(
            name = "documentId",
            description = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_ID_DESCRIPTION,
            example = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_ID_EXAMPLE,
            required = true,
        )
        @PathVariable("documentId")
        documentId: String,
    ): ResponseEntity<DocumentMetaInfoEntity>

    /**
     * Search for document meta information by document ID. Only results with QA status "Accepted" are returned.
     * @param companyId The company ID to filter by, ignored if null.
     * @param documentCategories The document categories to filter by, ignored if null.
     * @param reportingPeriod The reporting period to filter by, ignored if null.
     * @param chunkSize The maximum size of the chunk of search results returned. If
     * null, all search results are returned.
     * @param chunkIndex The index, counting started at 0, of the chunk that shall be
     * returned.
     * @return A ResponseEntity wrapping a list of DocumentUploadResponse objects.
     */
    @Operation(
        summary = "Search for document meta information.",
        description =
            "Search for document metainformation by company ID, document categories and reporting period. " +
                "Results are returned sorted by publication date in reverse chronological order. Only results" +
                "with QA status 'Accepted' are returned.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully searched for document meta information.",
            ),
            ApiResponse(
                responseCode = "400",
                description =
                    "Bad request; make sure that at least one search parameter is non-null and " +
                        "that the chunk index is within bounds (when in doubt, use chunk index 0).",
                content = [
                    Content(
                        schema = Schema(),
                    ),
                ],
            ),
        ],
    )
    @GetMapping(
        value = ["/"],
        produces = ["application/json"],
    )
    fun searchForDocumentMetaInformation(
        @Parameter(
            name = "companyId",
            description = DocumentManagerOpenApiDescriptionsAndExamples.COMPANY_ID_SEARCH_PARAMETER_DESCRIPTION,
            example = DocumentManagerOpenApiDescriptionsAndExamples.COMPANY_ID_SEARCH_PARAMETER_EXAMPLE,
            required = false,
        )
        @RequestParam
        companyId: String? = null,
        @Parameter(
            name = "documentCategories",
            description = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_CATEGORIES_SEARCH_PARAMETER_DESCRIPTION,
            required = false,
        )
        @RequestParam
        documentCategories: Set<DocumentCategory>? = null,
        @Parameter(
            name = "reportingPeriod",
            description = DocumentManagerOpenApiDescriptionsAndExamples.REPORTING_PERIOD_SEARCH_PARAMETER_DESCRIPTION,
            example = DocumentManagerOpenApiDescriptionsAndExamples.REPORTING_PERIOD_SEARCH_PARAMETER_EXAMPLE,
            required = false,
        )
        @RequestParam
        reportingPeriod: String? = null,
        @Parameter(
            name = "chunkSize",
            description = GeneralOpenApiDescriptionsAndExamples.CHUNK_SIZE_DESCRIPTION,
            required = false,
        )
        @RequestParam
        chunkSize: Int = 100,
        @Parameter(
            name = "chunkIndex",
            description = GeneralOpenApiDescriptionsAndExamples.CHUNK_INDEX_DESCRIPTION,
            required = false,
        )
        @RequestParam
        chunkIndex: Int = 0,
    ): ResponseEntity<List<DocumentMetaInfoResponse>>
}
