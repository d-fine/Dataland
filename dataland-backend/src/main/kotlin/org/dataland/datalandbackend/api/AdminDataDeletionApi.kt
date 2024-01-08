package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Defines the restful dataland-backend API regarding deleting data set
 */
@RequestMapping("/delete")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
fun interface AdminDataDeletionApi {

    /**
     * This method deletes data from storage
     * @param dataId filters the requested data to a specific entry.
     */
    @Operation(
        summary = "Delete specific data.",
        description = "Data identified by the provided data ID is deleted.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully deleted data set."),
        ],
    )
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    @DeleteMapping(
        value = ["/{dataId}"],
        produces = ["application/json"],
    )
    fun deleteDataSet(@PathVariable("dataId") dataId: String):
            ResponseEntity<String>
}
