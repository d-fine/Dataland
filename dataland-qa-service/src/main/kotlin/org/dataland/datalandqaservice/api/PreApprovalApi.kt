package org.dataland.datalandqaservice.org.dataland.datalandqaservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
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
            ApiResponse(responseCode = "403", description = "Only admins and judges can access pre-approval configuration."),
        ],
    )
    @GetMapping(
        value = ["/config"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_JUDGE')")
    fun getPreApprovalConfig(): ResponseEntity<PreApprovalConfig>

    /**
     * A method to update the pre-approval configuration.
     * @param newConfig the new configuration to apply
     */
    @Operation(
        summary = "Update the pre-approval configuration.",
        description = "Updates the pre-approval configuration. The sampling probability must be between 0.0 and 1.0.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully updated pre-approval configuration."),
            ApiResponse(responseCode = "400", description = "Invalid configuration values."),
            ApiResponse(responseCode = "403", description = "Only admins and judges can modify pre-approval configuration."),
        ],
    )
    @PatchMapping(
        value = ["/config"],
        produces = ["application/json"],
        consumes = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_JUDGE')")
    fun patchPreApprovalConfig(
        @RequestBody newConfig: PreApprovalConfig,
    ): ResponseEntity<PreApprovalConfig>
}
