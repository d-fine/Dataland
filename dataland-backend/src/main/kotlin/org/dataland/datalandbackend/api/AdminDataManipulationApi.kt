package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Defines the restful dataland-backend API regarding deletion endpoint for admin users
 */
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
@RequestMapping("/admin/data/{dataId}")
fun interface AdminDataManipulationApi {

    /**
     * A method to delete data via Dataland into a data store
     * @param dataId consisting of the ID of the dataset to be removed
     * @return meta info about the stored data including the ID of the created entry in the data store
     */
    @Operation(
        summary = "Delete a data set.",
        description = "The data is removed from the data store.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully removed data from the data store."),
        ],
    )
    @DeleteMapping(
        produces = ["application/json"],

    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun deleteCompanyAssociatedData(
        @PathVariable("dataId")
        @Valid
        @RequestBody
        dataId: String,
    ):
        ResponseEntity<String>
}
