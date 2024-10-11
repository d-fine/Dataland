package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.entities.DataMetaInformationForMyDatasets
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Defines the restful dataland-backend API regarding user data exchange
 */
@RequestMapping("/users")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
fun interface UserUploadsApi {
    /**
     * A method to retrieve dataset meta information uploaded by a specific user for the "My Datasets" page
     * @param userId the keycloak id of the user
     * @return a list of al uploaded by the specified user
     */
    @Operation(
        summary = "Retrieve an augmented dataset meta information uploaded by a specific user.",
        description =
        "Retrieve an augmented dataset meta information uploaded" +
            " by a specific user for the \"My Datasets\" page.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Successfully retrieved augmented dataset meta information.",
            ),
        ],
    )
    @GetMapping(
        value = ["/{userId}/uploads"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getUserUploadsDataMetaInformation(
        @PathVariable("userId") userId: String,
    ): ResponseEntity<List<DataMetaInformationForMyDatasets>>
}
