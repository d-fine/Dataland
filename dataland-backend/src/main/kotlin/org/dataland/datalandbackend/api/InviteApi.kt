package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.entities.InviteMetaInfoEntity
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

/**
 * Defines the Dataland-backend API regarding the creation of invites
 */

@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface InviteApi {
    @Operation(
        summary = "Create a Dataland invite.",
        description = "Create a Dataland invite by uploading an Excel file containing the invite info."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully created an invite.")
        ]
    )
    @PostMapping(
        value = ["/invite"],
        produces = ["application/json"],
        consumes = ["multipart/form-data"]
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    /**
     * A method to create an invite in Dataland by sending an Excel file which includes the invite info
     * @param excelFile is the Excel file which contains the invite info
     * @param isSubmitterNameHidden is a flag that decides if info about the submitters Dataland account shall be
     * included in the invite
     * @return a response object with info about the result and the success of the invite process
     */
    fun submitInvite(
        @RequestPart("excelFile") excelFile: MultipartFile,
        @RequestParam isSubmitterNameHidden: Boolean
    ):
        ResponseEntity<InviteMetaInfoEntity>
}
