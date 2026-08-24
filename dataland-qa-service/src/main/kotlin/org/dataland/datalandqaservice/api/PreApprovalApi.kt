package org.dataland.datalandqaservice.org.dataland.datalandqaservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfigPatchRequest
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfigPutRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Defines the restful API for pre-approval configuration.
 */
@RequestMapping("/pre-approval")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface PreApprovalApi {
    /**
     * A method to retrieve the current pre-approval configuration.
     */
    @Operation(
        summary = "Get the current pre-approval configuration.",
        description = "Get the current pre-approval configuration.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved pre-approval configuration."),
            ApiResponse(responseCode = "403", description = "Only admins can access pre-approval configuration."),
        ],
    )
    @GetMapping(
        value = ["/config"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getPreApprovalConfig(): ResponseEntity<PreApprovalConfig>

    /**
     * A method to partially update the pre-approval configuration.
     * @param patch the partial update to apply; absent (null) fields are left unchanged
     */
    @Operation(
        summary = "Update the pre-approval configuration.",
        description =
            "Partially updates the pre-approval configuration. Only the provided fields are changed. " +
                "The sampling probability must be between 0.0 and 1.0.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully updated pre-approval configuration."),
            ApiResponse(responseCode = "400", description = "Invalid configuration values."),
            ApiResponse(responseCode = "403", description = "Only admins can modify pre-approval configuration."),
        ],
    )
    @PatchMapping(
        value = ["/config"],
        produces = ["application/json"],
        consumes = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun patchPreApprovalConfig(
        @Valid @RequestBody patch: PreApprovalConfigPatchRequest,
    ): ResponseEntity<PreApprovalConfig>

    /**
     * A method to fully replace the pre-approval configuration.
     * @param newConfig the full configuration to replace the current one with
     */
    @Operation(
        summary = "Replace the pre-approval configuration.",
        description =
            "Fully replaces the pre-approval configuration with the provided one. All fields are required. " +
                "The sampling probability must be between 0.0 and 1.0.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully replaced pre-approval configuration."),
            ApiResponse(responseCode = "400", description = "Invalid configuration values."),
            ApiResponse(responseCode = "403", description = "Only admins can modify pre-approval configuration."),
        ],
    )
    @PutMapping(
        value = ["/config"],
        produces = ["application/json"],
        consumes = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun putPreApprovalConfig(
        @Valid @RequestBody newConfig: PreApprovalConfigPutRequest,
    ): ResponseEntity<PreApprovalConfig>
}
