package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.model.ExcelFilesUploadResponse
import org.dataland.datalandbackend.model.RequestMetaData
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
@SecurityRequirement(name = "default-oauth") // TODO I think we can leave this here to be able to use swagger UI
interface FileApi {
    @Operation(
        summary = "Create a Dataland invite.",
        description = "Excel files with invite info are processed."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully created an invite.")
        ]
    )
    @PostMapping(
        value = ["/invite"], // TODO name => think about it again
        produces = ["application/json"],
        consumes = ["multipart/form-data"]
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    /**
     * A method to create an invite in Dataland by sending Excel files which include the invite info
     * @param excelFiles are the Excel files which contain the invite info
     * @param isRequesterNameHidden is a flag that decides if info about the requesters Dataland account shall be
     * included in the invite
     * @return a response object with info about the result and the success of the invite process
     */
    fun submitInvitation(
        @RequestPart("excelFiles") excelFiles: List<MultipartFile>,
        @RequestParam isRequesterNameHidden: Boolean
    ):
        ResponseEntity<ExcelFilesUploadResponse>

}
