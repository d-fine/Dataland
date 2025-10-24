package org.dataland.datasourcingservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingWithoutReferences
import org.dataland.datasourcingservice.model.datasourcing.ReducedDataSourcing
import org.dataland.datasourcingservice.model.datasourcing.StoredDataSourcing
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

/**
 * API interface for handling data-sourcing operations.
 */
@RequestMapping("/data-sourcing")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface DataSourcingApi {
    /**
     * Retrieve a DataSourcing object by its ID.
     */
    @Operation(
        summary = "Get DataSourcing by ID",
        description = "Retrieve a DataSourcing object by its unique identifier.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved DataSourcing object."),
            ApiResponse(
                responseCode = "403",
                description = "Only admins are allowed to query data sourcing objects.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "DataSourcing object not found.",
                content = [Content(schema = Schema())],
            ),
        ],
    )
    @GetMapping(value = ["/{dataSourcingId}"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getDataSourcingById(
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_DESCRIPTION,
            example = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_EXAMPLE,
        )
        @PathVariable dataSourcingId: String,
    ): ResponseEntity<StoredDataSourcing>

    /**
     * Retrieve a DataSourcing object by the company ID of the
     * external service provider (document collector and/or data extractor)
     */
    @Operation(
        summary = "Get DataSourcing by company ID of the provider (document collector and/or data extractor)",
        description =
            "Get DataSourcing by company ID of the assigned external service provider " +
                "(document collector and/or data extractor).",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved DataSourcing objects."),
            ApiResponse(
                responseCode = "403",
                description = "Only uploaders are allowed to use this endpoint.",
                content = [Content(array = ArraySchema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "DataSourcing object not found.",
                content = [Content(array = ArraySchema())],
            ),
        ],
    )
    @GetMapping(value = ["/provider/{providerCompanyId}"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun getDataSourcingForCompanyId(
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.PROVIDER_COMPANY_ID_DESCRIPTION,
            example = DataSourcingOpenApiDescriptionsAndExamples.PROVIDER_COMPANY_ID_EXAMPLE,
        )
        @PathVariable providerCompanyId: String,
    ): ResponseEntity<List<ReducedDataSourcing>>

    /**
     * Retrieve the history of a DataSourcing object by its ID.
     */
    @Operation(
        summary = "Get full history of a DataSourcing by ID",
        description = "Retrieve the history of a DataSourcing object by its unique identifier.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved DataSourcing history."),
            ApiResponse(
                responseCode = "403",
                description = "Only admins are allowed to query data sourcing history.",
                content = [Content(array = ArraySchema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "DataSourcing object not found.",
                content = [Content(array = ArraySchema())],
            ),
        ],
    )
    @GetMapping(value = ["/{dataSourcingId}/history"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getDataSourcingHistoryById(
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_DESCRIPTION,
            example = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_EXAMPLE,
        )
        @PathVariable
        dataSourcingId: String,
    ): ResponseEntity<List<DataSourcingWithoutReferences>>

    /**
     * Patch the state of a DataSourcing object specified by ID.
     */
    @Operation(
        summary = "Patch DataSourcing state",
        description = "Patch the state of a DataSourcing object specified by ID.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully patched state."),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland Uploaders have the right to patch states of data sourcing objects.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "DataSourcing object not found.",
                content = [Content(schema = Schema())],
            ),
        ],
    )
    @PatchMapping(value = ["/{dataSourcingId}/state"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun patchDataSourcingState(
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_DESCRIPTION,
            example = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_EXAMPLE,
        )
        @PathVariable
        dataSourcingId: String,
        @Valid @RequestParam state: DataSourcingState,
    ): ResponseEntity<ReducedDataSourcing>

    /**
     * Patch providers (document collector, data extractor) and/or admin comment of a DataSourcing object specified by ID.
     */
    @Operation(
        summary = "Patch provider and/or admin comment",
        description =
            "Patch the providers (document collector, data extractor) and/or admin comment of a DataSourcing object " +
                "specified by ID. Null values are ignored.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully patched providers and/or admin comment.",
            ),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland Admins to patch document collectors and data extractors.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "DataSourcing object not found.",
                content = [Content(schema = Schema())],
            ),
        ],
    )
    @PatchMapping(value = ["/{dataSourcingId}"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun patchProviderAndAdminComment(
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_DESCRIPTION,
            example = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_EXAMPLE,
        )
        @PathVariable
        dataSourcingId: String,
        @Valid
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.DOCUMENT_COLLECTOR_DESCRIPTION,
            example = DataSourcingOpenApiDescriptionsAndExamples.DOCUMENT_COLLECTOR_EXAMPLE,
        )
        @RequestParam
        documentCollector: String?,
        @Valid
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.DATA_EXTRACTOR_DESCRIPTION,
            example = DataSourcingOpenApiDescriptionsAndExamples.DATA_EXTRACTOR_EXAMPLE,
        )
        @RequestParam
        dataExtractor: String?,
        @Valid
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.ADMIN_COMMENT_DESCRIPTION,
            example = DataSourcingOpenApiDescriptionsAndExamples.ADMIN_COMMENT_EXAMPLE,
        )
        @RequestParam
        adminComment: String?,
    ): ResponseEntity<StoredDataSourcing>

    /**
     * Patch the associated document IDs of a DataSourcing object. Use appendDocuments to append or overwrite.
     */
    @Operation(
        summary = "Patch DataSourcing documents",
        description =
            "Patch the associated document IDs of a DataSourcing object. " +
                "If appendDocuments is set to true, the provided document IDs are appended to the existing ones. " +
                "Otherwise, they overwrite the existing ones. If omitted, appendDocuments is treated as true.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully patched document IDs."),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland Uploaders have the right to patch documents associated with data sourcing objects.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "DataSourcing object not found.",
                content = [Content(schema = Schema())],
            ),
        ],
    )
    @PatchMapping(
        value = ["/{dataSourcingId}/documents"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun patchDataSourcingDocuments(
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_DESCRIPTION,
            example = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_EXAMPLE,
        )
        @PathVariable
        dataSourcingId: String,
        @ArraySchema(
            arraySchema =
                Schema(
                    type = "string",
                    description = DataSourcingOpenApiDescriptionsAndExamples.DOCUMENT_IDS_PATCH_DESCRIPTION,
                    example = DataSourcingOpenApiDescriptionsAndExamples.DOCUMENT_IDS_EXAMPLE,
                ),
        )
        @RequestBody
        documentIds: Set<String>,
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.APPEND_DOCUMENTS_DESCRIPTION,
        )
        @RequestParam(name = "appendDocuments", defaultValue = "true")
        appendDocuments: Boolean = true,
    ): ResponseEntity<ReducedDataSourcing>

    /**
     * Patch the dateDocumentSourcingAttempt field of a DataSourcing object for a given ID. Accepts a list of dates.
     */
    @Operation(
        summary = "Patch the date of the next planned document sourcing attempt",
        description = "Patch the date of the next planned document sourcing attempt of a DataSourcing object for a given ID.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully patched the date of the next document sourcing attempt.",
            ),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland Uploaders have the right to patch the dates of document sourcing attempts.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "DataSourcing object not found.",
                content = [Content(schema = Schema())],
            ),
        ],
    )
    @PatchMapping(value = ["/{dataSourcingId}/document-sourcing-attempt"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun patchDateOfNextDocumentSourcingAttempt(
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_DESCRIPTION,
            example = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_EXAMPLE,
        )
        @PathVariable
        dataSourcingId: String,
        @Valid
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.DATE_OF_NEXT_DOCUMENT_SOURCING_ATTEMPT_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.GENERAL_DATE_EXAMPLE,
        )
        @RequestParam
        dateOfNextDocumentSourcingAttempt: LocalDate,
    ): ResponseEntity<ReducedDataSourcing>

    /**
     * Search for DataSourcing objects based on various parameters.
     */
    @Operation(
        summary = "Search data sourcings.",
        description = "Search for data sourcing objects using various filter parameters.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved matching data sourcing objects."),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland admins have the right to search for data sourcing objects.",
                content = [Content(array = ArraySchema())],
            ),
        ],
    )
    @GetMapping(produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun searchDataSourcings(
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
        )
        @RequestParam(required = false)
        companyId: String? = null,
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_FRAMEWORK_EXAMPLE,
        )
        @RequestParam(required = false)
        dataType: String? = null,
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
        )
        @RequestParam(required = false)
        reportingPeriod: String? = null,
        @Parameter(
            description = DataSourcingOpenApiDescriptionsAndExamples.STATE_DESCRIPTION,
        )
        state: DataSourcingState? = null,
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.CHUNK_SIZE_DESCRIPTION,
            required = false,
        )
        @RequestParam(defaultValue = "100")
        chunkSize: Int,
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.CHUNK_INDEX_DESCRIPTION,
            required = false,
        )
        @RequestParam(defaultValue = "0")
        chunkIndex: Int,
    ): ResponseEntity<List<StoredDataSourcing>>
}
