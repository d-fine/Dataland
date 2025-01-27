package org.dataland.datalandbackend.api

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the restful dataland-backend API regarding migration from stored datasets to assembled datasets.
 * This is an internal API
 */
@RequestMapping("/assembled-dataset-migration")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface AssembledDatasetMigrationApi {
    /**
     * A method to migrate data from stored datasets to assembled datasets
     * @param dataId to migrate
     */
    @Operation(
        summary = "Triggers the migration from stored datasets to assembled datasets.",
        description = "The data is migrated.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Migration successful."),
        ],
    )
    @PostMapping("/stored-dataset-to-assembled-dataset-migration/{dataId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun migrateStoredDatasetToAssembledDataset(
        @PathVariable("dataId")
        dataId: String,
    )

    /**
     * A method to force upload a dataset as a stored dataset for testing.
     * This endpoint is potentially dangerous and should only be used for testing purposes.
     */
    @Operation(
        summary = "Triggers a forced upload of a dataset as a stored dataset for testing.",
        description = "The data is stored.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Migration successful."),
        ],
    )
    @PostMapping("/upload-as-stored-dataset/{dataType}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun forceUploadDatasetAsStoredDataset(
        @PathVariable("dataType") dataType: DataType,
        @RequestBody companyAssociatedData: CompanyAssociatedData<JsonNode>,
        @RequestParam(defaultValue = "false")bypassQa: Boolean,
    ): ResponseEntity<DataMetaInformation>
}
