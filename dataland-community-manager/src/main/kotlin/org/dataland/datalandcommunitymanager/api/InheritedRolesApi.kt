package org.dataland.datalandcommunitymanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Defines restful Dataland Community Manager API regarding inherited roles.
 */
@RequestMapping("/inherited-roles")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface InheritedRolesApi {
    /**
     * For the specified user, get a mapping from company IDs of companies of which the user has a CompanyRole
     * to the lists of associated InheritedRoles of the user.
     */
    @Operation(
        summary = "Get inherited roles for a user.",
        description = "For the specified user, get a mapping from company IDs to the lists of associated inherited roles.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved inherited roles for the user."),
            ApiResponse(
                responseCode = "403",
                description = "Only Dataland admins may query inherited roles.",
                content = [Content(schema = Schema())],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Dataland does not know the specified user ID.",
                content = [Content(schema = Schema())],
            ),
        ],
    )
    @GetMapping(
        value = ["/{userId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getInheritedRoles(
        @PathVariable("userId") userId: String,
    ): ResponseEntity<Map<String, List<String>>>
}
