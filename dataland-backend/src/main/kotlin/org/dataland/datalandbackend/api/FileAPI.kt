package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.model.ExcelFileUploadResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.multipart.MultipartFile

/**
 * Defines the Dataland-backend API regarding the exchange of files
 */

@RequestMapping("/file") // TODO Philip: Change name to smth more specific (like /invite   or similar)
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "dataland-api-key") // TODO I think no need
@SecurityRequirement(name = "default-oauth") // TODO I think no need
interface FileAPI {
    @Operation(
        summary = "Upload an Excel file.",
        description = "An Excel file is uploaded to Dataland and is stored."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully stored Excel file.")
        ]
    )
    @PostMapping(
        value = ["/excel"], // TODO name (see TODO comment above)
        produces = ["application/json"],
        consumes = ["application/vnd.ms-excel"] // TODO https://www.baeldung.com/sprint-boot-multipart-requests
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    /**
     * A method to store an Excel file in Dataland
     * @param excelFile is the Excel file which needs to be stored
     * @return a response object with info about the result and the success of the upload process
     */
    fun uploadExcelFile(excelFile: MultipartFile):
        ResponseEntity<ExcelFileUploadResponse>
}
