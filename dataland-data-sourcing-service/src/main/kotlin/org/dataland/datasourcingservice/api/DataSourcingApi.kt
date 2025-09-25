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
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
@RequestMapping("/data-sourcing")
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
    @GetMapping("/{id}", produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getDataSourcingById(
        @Parameter(description = "ID of the DataSourcing object.")
        @PathVariable id: String,
    ): ResponseEntity<StoredDataSourcing>

    /**
     * Retrieve a DataSourcing object by its ID.
     */
    @Operation(
        summary = "Get DataSourcing by ID",
        description = "Retrieve DataSourcing objects assigned to your company.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved DataSourcing objects."),
            ApiResponse(
                responseCode = "403",
                description = "Only uploaders are allowed to use this endpoint.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "DataSourcing object not found.",
                content = [Content(schema = Schema())],
            ),
        ],
    )
    @GetMapping("/{companyId}", produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun getDataSourcingForCompanyId(
        @Parameter(description = "ID of the document collector or data extractor.")
        @PathVariable companyId: String,
    ): ResponseEntity<List<StoredDataSourcing>>

    /**
     * Retrieve a DataSourcing object by reporting period, dataType, companyId, and optionally state.
     */
    @Operation(
        summary = "Get DataSourcing by parameters",
        description = "Retrieve a DataSourcing object by reporting period, dataType, companyId, and optionally state.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved DataSourcing object."),
            ApiResponse(
                responseCode = "403",
                description = "Only admins are allowed to query DataSourcing objects.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "DataSourcing object not found.",
                content = [Content(schema = Schema())],
            ),
        ],
    )
    @GetMapping("/search", produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getDataSourcingByDimensions(
        @Parameter(description = "Company ID.") @RequestParam companyId: String,
        @Parameter(description = "Data type.") @RequestParam dataType: String,
        @Parameter(description = "Reporting period.") @RequestParam reportingPeriod: String,
    ): ResponseEntity<StoredDataSourcing>

    /**
     * Retrieve the history of a DataSourcing object by its ID.
     */
    @Operation(
        summary = "Get full history of DataSourcing history by ID",
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
    @GetMapping("/{id}/history", produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getDataSourcingHistoryById(
        @Parameter(description = "ID of the DataSourcing object.")
        @PathVariable id: String,
    ): ResponseEntity<List<StoredDataSourcing>>

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
    @PatchMapping("/{id}/state", consumes = ["application/json"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun patchDataSourcingState(
        @Parameter(description = "ID of the DataSourcing object.") @PathVariable id: String,
        @Valid @RequestParam state: DataSourcingState,
    ): ResponseEntity<StoredDataSourcing>

    /**
     * Patch the state of a DataSourcing object specified by ID.
     */
    @Operation(
        summary = "Patch document collector and/or data extractor. ",
        description =
            "Patch the document collector and/or data extractor of a DataSourcing object specified by ID." +
                " Null values are ignored. Optionally: Provide an admin comment.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully patched document collector and/or data extractor."),
            ApiResponse(responseCode = "404", description = "DataSourcing object not found."),
        ],
    )
    @PatchMapping("/{id}/assign", consumes = ["application/json"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun patchDocumentCollectorAndDataExtractor(
        @Parameter(description = "ID of the DataSourcing object.") @PathVariable id: String,
        @Valid @RequestParam documentCollector: String?,
        @Valid @RequestParam dataExtractor: String?,
        @Valid @RequestParam adminComment: String?,
    ): ResponseEntity<ReducedDataSourcing>

    /**
     * Patch the associated document IDs of a DataSourcing object. Use appendDocuments to append or overwrite.
     */
    @Operation(
        summary = "Patch DataSourcing documents",
        description = "Patch the associated document IDs of a DataSourcing object. Use appendDocuments to append or overwrite.",
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
    @PatchMapping("/{id}/documents", consumes = ["application/json"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun patchDataSourcingDocuments(
        @Parameter(description = "ID of the DataSourcing object.") @PathVariable id: String,
        @Valid @RequestBody documentIds: Set<String>,
        @Valid @RequestParam(name = "appendDocuments", defaultValue = "true") appendDocuments: Boolean = true,
    ): ResponseEntity<StoredDataSourcing>

    /**
     * Patch the dateDocumentSourcingAttempt field of a DataSourcing object for a given ID. Accepts a list of dates.
     */
    @Operation(
        summary = "Patch the date of the next planned document sourcing attempt",
        description = "Patch the date of the next planned document sourcing attempt of a DataSourcing object for a given ID.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully patched dateDocumentSourcingAttempt."),
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
    @PatchMapping("/{id}/document-sourcing-attempt", consumes = ["application/json"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun patchDateDocumentSourcingAttempt(
        @Parameter(description = "ID of the DataSourcing object.") @PathVariable id: String,
        @Valid @RequestParam date: LocalDate,
    ): ResponseEntity<StoredDataSourcing>
}
