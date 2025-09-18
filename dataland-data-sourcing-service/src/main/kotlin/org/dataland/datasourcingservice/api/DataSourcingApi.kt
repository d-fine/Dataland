package org.dataland.datasourcingservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datasourcingservice.model.DataSourcingResponse
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.Date
import java.util.SortedSet

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
    @Operation(summary = "Get DataSourcing by ID", description = "Retrieve a DataSourcing object by its unique identifier.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved DataSourcing object."),
            ApiResponse(responseCode = "404", description = "DataSourcing object not found."),
        ],
    )
    @GetMapping("/{id}", produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getDataSourcingById(
        @Parameter(description = "ID of the DataSourcing object.")
        @PathVariable id: String,
    ): ResponseEntity<DataSourcingResponse>

    /**
     * Retrieve a DataSourcing object by reporting period, dataType, companyId, and optionally state.
     * Todo: What the point of specifying state here? There can't be multiple entries with same reportingPeriod, dataType, companyId.
     */
    @Operation(
        summary = "Get DataSourcing by parameters",
        description = "Retrieve a DataSourcing object by reporting period, dataType, companyId, and optionally state.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved DataSourcing object."),
            ApiResponse(responseCode = "404", description = "DataSourcing object not found."),
        ],
    )
    @GetMapping("/search", produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getDataSourcingByDimensions(
        @Parameter(description = "Company ID.") @RequestParam companyId: String,
        @Parameter(description = "Data type.") @RequestParam dataType: String,
        @Parameter(description = "Reporting period.") @RequestParam reportingPeriod: String,
        @Parameter(description = "State (optional).") @RequestParam(required = false) state: String?,
    ): ResponseEntity<DataSourcingResponse>

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
            ApiResponse(responseCode = "404", description = "DataSourcing object not found."),
        ],
    )
    @GetMapping("/{id}/history", produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getDataSourcingHistoryById(
        @Parameter(description = "ID of the DataSourcing object.")
        @PathVariable id: String,
    ): ResponseEntity<List<DataSourcingResponse>>

    /**
     * Patch the state of a DataSourcing object specified by ID.
     */
    @Operation(summary = "Patch DataSourcing state", description = "Patch the state of a DataSourcing object specified by ID.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully patched state."),
            ApiResponse(responseCode = "404", description = "DataSourcing object not found."),
        ],
    )
    @PatchMapping("/{id}/state", consumes = ["application/json"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun patchDataSourcingState(
        @Parameter(description = "ID of the DataSourcing object.") @PathVariable id: String,
        @Valid @RequestParam state: DataSourcingState,
    ): ResponseEntity<DataSourcingResponse>

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
            ApiResponse(responseCode = "404", description = "DataSourcing object not found."),
        ],
    )
    @PatchMapping("/{id}/documents", consumes = ["application/json"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun patchDataSourcingDocuments(
        @Parameter(description = "ID of the DataSourcing object.") @PathVariable id: String,
        @Valid @RequestParam(name = "appendDocuments", defaultValue = "true") appendDocuments: Boolean = true,
        @Valid @RequestBody documentIds: Set<String>,
    ): ResponseEntity<DataSourcingResponse>

    /**
     * Patch the dateDocumentSourcingAttempt field of a DataSourcing object for a given ID. Accepts a list of dates.
     * Todo: What is the purpose of this endpoint? The entity has a single date field, but this accepts a list of dates.
     */
    @Operation(
        summary = "Patch dateDocumentSourcingAttempt",
        description = "Patch the dateDocumentSourcingAttempt field of a DataSourcing object for a given ID. Accepts a list of dates.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully patched dateDocumentSourcingAttempt."),
            ApiResponse(responseCode = "404", description = "DataSourcing object not found."),
        ],
    )
    @PatchMapping("/{id}/document-sourcing-attempt", consumes = ["application/json"], produces = ["application/json"])
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun patchDateDocumentSourcingAttempt(
        @Parameter(description = "ID of the DataSourcing object.") @PathVariable id: String,
        @Valid @RequestBody request: SortedSet<Date>,
    ): ResponseEntity<DataSourcingResponse>
}
