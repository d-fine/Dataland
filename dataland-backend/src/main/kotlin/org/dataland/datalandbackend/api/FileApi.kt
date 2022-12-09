package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.model.ExcelFilesUploadResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

/**
 * Defines the Dataland-backend API regarding the exchange of files
 */

@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "dataland-api-key") // TODO I think no need
@SecurityRequirement(name = "default-oauth") // TODO I think no need
interface FileApi {
    @Operation(
        summary = "Upload Excel files.",
        description = "Excel files are uploaded to Dataland and are stored."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully stored Excel files.")
        ]
    )
    @PostMapping(
        value = ["/invite"], // TODO name => think about it again
        produces = ["application/json"],
        consumes = ["multipart/form-data"] // TODO https://www.baeldung.com/sprint-boot-multipart-requests
    )
    // @PreAuthorize("hasRole('ROLE_USER')")
    /**
     * A method to store Excel files in Dataland
     * @param excelFiles are the Excel files which need to be stored
     * @return a response object with info about the result and the success of the upload process
     */
    fun uploadExcelFiles(@RequestParam("files") excelFiles: List<MultipartFile>):
        ResponseEntity<ExcelFilesUploadResponse>
}
